package com.marshall.pyerite.characterSkillsModule.model

/**
 * Expands compact plan targets into one [SkillPlanEntry] per finished level
 * (0→1, 1→2, …), with prerequisite skills ordered before dependents.
 */
internal object SkillPlanLevelStepExpander {

    /**
     * @param entries compact plan targets (skill → max level)
     * @param flattenedPrerequisitesFor recursive prereq map for a skill (excludes itself)
     * @param directPrerequisitesFor immediate prereq map for topo edges
     */
    fun expand(
        entries: List<SkillPlanEntry>,
        flattenedPrerequisitesFor: (skillTypeId: Int) -> Map<Int, Int>,
        directPrerequisitesFor: (skillTypeId: Int) -> Map<Int, Int>,
    ): List<SkillPlanEntry> {
        if (entries.isEmpty()) return emptyList()

        val targets = linkedMapOf<Int, Int>()
        for (entry in entries) {
            val level = entry.targetLevel.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
            targets[entry.skillTypeId] = maxOf(targets[entry.skillTypeId] ?: 0, level)
        }

        val plannedSkillIds = targets.keys.toList()
        for (skillTypeId in plannedSkillIds) {
            flattenedPrerequisitesFor(skillTypeId).forEach { (prereqId, requiredLevel) ->
                val level = requiredLevel.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
                targets[prereqId] = maxOf(targets[prereqId] ?: 0, level)
            }
        }

        val orderedSkillIds = topologicalSkillOrder(
            skillIds = targets.keys,
            directPrerequisitesFor = directPrerequisitesFor,
        )
        return orderedSkillIds.flatMap { skillTypeId ->
            val targetLevel = targets.getValue(skillTypeId)
            (1..targetLevel).map { level ->
                SkillPlanEntry(skillTypeId = skillTypeId, targetLevel = level)
            }
        }
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

        // Cycles / missing edges: append leftovers in original order.
        if (ordered.size < skillIds.size) {
            for (skillId in skillIds) {
                if (skillId !in ordered) ordered.add(skillId)
            }
        }
        return ordered
    }
}
