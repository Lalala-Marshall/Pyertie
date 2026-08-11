package com.marshall.pyerite.characterSkillsModule.model

/**
 * Expands compact plan targets into one [SkillPlanEntry] per finished level
 * (0→1, 1→2, …).
 *
 * Order follows **plan entry add order**: for each compact entry in list order,
 * emit its missing prerequisites (topo-sorted), then that skill’s level steps.
 * Example: add X (prereq Y), then Z (prereq L) → Y, X, L, Z.
 */
internal object SkillPlanLevelStepExpander {

    /**
     * Orders compact targets for a single add batch: prerequisites before each
     * selected skill, preserving [levelsBySkillTypeId] iteration order across roots.
     */
    fun orderCompactTargets(
        levelsBySkillTypeId: Map<Int, Int>,
        flattenedPrerequisitesFor: (skillTypeId: Int) -> Map<Int, Int>,
        directPrerequisitesFor: (skillTypeId: Int) -> Map<Int, Int>,
    ): List<SkillPlanEntry> {
        if (levelsBySkillTypeId.isEmpty()) return emptyList()

        val ordered = linkedMapOf<Int, Int>()

        fun ensure(skillTypeId: Int, level: Int) {
            val target = level.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
            if (target <= 0) return
            val prereqs = flattenedPrerequisitesFor(skillTypeId)
            if (prereqs.isNotEmpty()) {
                val prereqOrder = topologicalSkillOrder(
                    skillIds = prereqs.keys,
                    directPrerequisitesFor = directPrerequisitesFor,
                )
                for (prereqId in prereqOrder) {
                    val required = prereqs[prereqId] ?: continue
                    ensure(prereqId, required)
                }
            }
            ordered[skillTypeId] = maxOf(ordered[skillTypeId] ?: 0, target)
        }

        for ((skillTypeId, level) in levelsBySkillTypeId) {
            if (level > 0) ensure(skillTypeId, level)
        }
        return ordered.map { (skillTypeId, targetLevel) ->
            SkillPlanEntry(skillTypeId = skillTypeId, targetLevel = targetLevel)
        }
    }

    /**
     * @param entries compact plan targets in add order (skill → max level)
     * @param flattenedPrerequisitesFor recursive prereq map for a skill (excludes itself)
     * @param directPrerequisitesFor immediate prereq map for topo edges
     */
    fun expand(
        entries: List<SkillPlanEntry>,
        flattenedPrerequisitesFor: (skillTypeId: Int) -> Map<Int, Int>,
        directPrerequisitesFor: (skillTypeId: Int) -> Map<Int, Int>,
    ): List<SkillPlanEntry> {
        if (entries.isEmpty()) return emptyList()

        val emittedUpTo = mutableMapOf<Int, Int>()
        val steps = ArrayList<SkillPlanEntry>()

        fun emitUpTo(skillTypeId: Int, level: Int) {
            val target = level.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
            val from = (emittedUpTo[skillTypeId] ?: 0) + 1
            if (from > target) return
            for (finishedLevel in from..target) {
                steps += SkillPlanEntry(
                    skillTypeId = skillTypeId,
                    targetLevel = finishedLevel,
                )
            }
            emittedUpTo[skillTypeId] = target
        }

        fun emitPrerequisitesThenSkill(skillTypeId: Int, level: Int) {
            val prereqs = flattenedPrerequisitesFor(skillTypeId)
            if (prereqs.isNotEmpty()) {
                val prereqOrder = topologicalSkillOrder(
                    skillIds = prereqs.keys,
                    directPrerequisitesFor = directPrerequisitesFor,
                )
                for (prereqId in prereqOrder) {
                    val required = prereqs[prereqId] ?: continue
                    emitUpTo(prereqId, required)
                }
            }
            emitUpTo(skillTypeId, level)
        }

        for (entry in entries) {
            emitPrerequisitesThenSkill(
                skillTypeId = entry.skillTypeId,
                level = entry.targetLevel,
            )
        }
        return steps
    }

    /**
     * Kahn topo-sort: prerequisites before dependents. Skills with equal priority
     * keep the encounter order from [skillIds].
     */
    private fun topologicalSkillOrder(
        skillIds: Set<Int>,
        directPrerequisitesFor: (skillTypeId: Int) -> Map<Int, Int>,
    ): List<Int> {
        if (skillIds.isEmpty()) return emptyList()

        val indegree = linkedMapOf<Int, Int>()
        val dependents = linkedMapOf<Int, MutableList<Int>>()
        for (skillId in skillIds) {
            indegree[skillId] = 0
            dependents[skillId] = mutableListOf()
        }

        for (skillId in skillIds) {
            val prereqIds = directPrerequisitesFor(skillId).keys.filter { it in skillIds }
            for (prereqId in prereqIds) {
                dependents.getValue(prereqId).add(skillId)
                indegree[skillId] = indegree.getValue(skillId) + 1
            }
        }

        val ready = ArrayDeque<Int>()
        for ((skillId, degree) in indegree) {
            if (degree == 0) ready.addLast(skillId)
        }

        val ordered = ArrayList<Int>(skillIds.size)
        while (ready.isNotEmpty()) {
            val skillId = ready.removeFirst()
            ordered.add(skillId)
            for (dependentId in dependents.getValue(skillId)) {
                val next = indegree.getValue(dependentId) - 1
                indegree[dependentId] = next
                if (next == 0) ready.addLast(dependentId)
            }
        }

        if (ordered.size < skillIds.size) {
            for (skillId in skillIds) {
                if (skillId !in ordered) ordered.add(skillId)
            }
        }
        return ordered
    }
}
