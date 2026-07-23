package com.marshall.pyerite.eveAuthModule

/**
 * EVE SSO / ESI OAuth scopes.
 *
 * - [apiValue] is the wire string sent to SSO and stored on the token.
 * - [category] groups scopes for future UI (feature gates, permission lists).
 * Unknown CCP scopes stay as raw strings in storage; use [fromApiValue] / [parseGranted].
 */
enum class EveSsoScope(
    val apiValue: String,
    val category: EveSsoScopeCategory,
) {
    PUBLIC_DATA("publicData", EveSsoScopeCategory.PUBLIC),

    CALENDAR_RESPOND("esi-calendar.respond_calendar_events.v1", EveSsoScopeCategory.CALENDAR),
    CALENDAR_READ("esi-calendar.read_calendar_events.v1", EveSsoScopeCategory.CALENDAR),

    LOCATION_READ("esi-location.read_location.v1", EveSsoScopeCategory.LOCATION),
    LOCATION_SHIP_TYPE("esi-location.read_ship_type.v1", EveSsoScopeCategory.LOCATION),
    LOCATION_ONLINE("esi-location.read_online.v1", EveSsoScopeCategory.LOCATION),

    MAIL_ORGANIZE("esi-mail.organize_mail.v1", EveSsoScopeCategory.MAIL),
    MAIL_READ("esi-mail.read_mail.v1", EveSsoScopeCategory.MAIL),
    MAIL_SEND("esi-mail.send_mail.v1", EveSsoScopeCategory.MAIL),

    SKILLS_READ("esi-skills.read_skills.v1", EveSsoScopeCategory.SKILLS),
    SKILLQUEUE_READ("esi-skills.read_skillqueue.v1", EveSsoScopeCategory.SKILLS),

    WALLET_CHARACTER("esi-wallet.read_character_wallet.v1", EveSsoScopeCategory.WALLET),
    WALLET_CORPORATION("esi-wallet.read_corporation_wallet.v1", EveSsoScopeCategory.WALLET),
    WALLET_CORPORATION_WALLETS("esi-wallet.read_corporation_wallets.v1", EveSsoScopeCategory.WALLET),

    SEARCH_STRUCTURES("esi-search.search_structures.v1", EveSsoScopeCategory.SEARCH),

    CLONES_READ("esi-clones.read_clones.v1", EveSsoScopeCategory.CLONES),
    CLONES_IMPLANTS("esi-clones.read_implants.v1", EveSsoScopeCategory.CLONES),

    CHARACTERS_READ_CONTACTS("esi-characters.read_contacts.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_WRITE_CONTACTS("esi-characters.write_contacts.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_LOYALTY("esi-characters.read_loyalty.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_CHAT_CHANNELS("esi-characters.read_chat_channels.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_MEDALS("esi-characters.read_medals.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_STANDINGS("esi-characters.read_standings.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_AGENTS_RESEARCH("esi-characters.read_agents_research.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_BLUEPRINTS("esi-characters.read_blueprints.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_CORPORATION_ROLES("esi-characters.read_corporation_roles.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_FATIGUE("esi-characters.read_fatigue.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_NOTIFICATIONS("esi-characters.read_notifications.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_TITLES("esi-characters.read_titles.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_FW_STATS("esi-characters.read_fw_stats.v1", EveSsoScopeCategory.CHARACTERS),
    CHARACTERS_READ_FREELANCE_JOBS("esi-characters.read_freelance_jobs.v1", EveSsoScopeCategory.CHARACTERS),

    UNIVERSE_READ_STRUCTURES("esi-universe.read_structures.v1", EveSsoScopeCategory.UNIVERSE),

    KILLMAILS_READ("esi-killmails.read_killmails.v1", EveSsoScopeCategory.KILLMAILS),
    KILLMAILS_READ_CORPORATION("esi-killmails.read_corporation_killmails.v1", EveSsoScopeCategory.KILLMAILS),

    CORPORATIONS_READ_MEMBERSHIP(
        "esi-corporations.read_corporation_membership.v1",
        EveSsoScopeCategory.CORPORATIONS,
    ),
    CORPORATIONS_READ_STRUCTURES("esi-corporations.read_structures.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_TRACK_MEMBERS("esi-corporations.track_members.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_DIVISIONS("esi-corporations.read_divisions.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_CONTACTS("esi-corporations.read_contacts.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_TITLES("esi-corporations.read_titles.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_BLUEPRINTS("esi-corporations.read_blueprints.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_STANDINGS("esi-corporations.read_standings.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_STARBASES("esi-corporations.read_starbases.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_CONTAINER_LOGS(
        "esi-corporations.read_container_logs.v1",
        EveSsoScopeCategory.CORPORATIONS,
    ),
    CORPORATIONS_READ_FACILITIES("esi-corporations.read_facilities.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_MEDALS("esi-corporations.read_medals.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_FW_STATS("esi-corporations.read_fw_stats.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_PROJECTS("esi-corporations.read_projects.v1", EveSsoScopeCategory.CORPORATIONS),
    CORPORATIONS_READ_FREELANCE_JOBS(
        "esi-corporations.read_freelance_jobs.v1",
        EveSsoScopeCategory.CORPORATIONS,
    ),

    ASSETS_READ("esi-assets.read_assets.v1", EveSsoScopeCategory.ASSETS),
    ASSETS_READ_CORPORATION("esi-assets.read_corporation_assets.v1", EveSsoScopeCategory.ASSETS),

    PLANETS_MANAGE("esi-planets.manage_planets.v1", EveSsoScopeCategory.PLANETS),
    PLANETS_READ_CUSTOMS_OFFICES("esi-planets.read_customs_offices.v1", EveSsoScopeCategory.PLANETS),

    FLEETS_READ("esi-fleets.read_fleet.v1", EveSsoScopeCategory.FLEETS),
    FLEETS_WRITE("esi-fleets.write_fleet.v1", EveSsoScopeCategory.FLEETS),

    UI_OPEN_WINDOW("esi-ui.open_window.v1", EveSsoScopeCategory.UI),
    UI_WRITE_WAYPOINT("esi-ui.write_waypoint.v1", EveSsoScopeCategory.UI),

    FITTINGS_READ("esi-fittings.read_fittings.v1", EveSsoScopeCategory.FITTINGS),
    FITTINGS_WRITE("esi-fittings.write_fittings.v1", EveSsoScopeCategory.FITTINGS),

    MARKETS_STRUCTURE("esi-markets.structure_markets.v1", EveSsoScopeCategory.MARKETS),
    MARKETS_CHARACTER_ORDERS("esi-markets.read_character_orders.v1", EveSsoScopeCategory.MARKETS),
    MARKETS_CORPORATION_ORDERS("esi-markets.read_corporation_orders.v1", EveSsoScopeCategory.MARKETS),

    INDUSTRY_CHARACTER_JOBS("esi-industry.read_character_jobs.v1", EveSsoScopeCategory.INDUSTRY),
    INDUSTRY_CORPORATION_JOBS("esi-industry.read_corporation_jobs.v1", EveSsoScopeCategory.INDUSTRY),
    INDUSTRY_CHARACTER_MINING("esi-industry.read_character_mining.v1", EveSsoScopeCategory.INDUSTRY),
    INDUSTRY_CORPORATION_MINING("esi-industry.read_corporation_mining.v1", EveSsoScopeCategory.INDUSTRY),

    CONTRACTS_CHARACTER("esi-contracts.read_character_contracts.v1", EveSsoScopeCategory.CONTRACTS),
    CONTRACTS_CORPORATION("esi-contracts.read_corporation_contracts.v1", EveSsoScopeCategory.CONTRACTS),

    ALLIANCES_READ_CONTACTS("esi-alliances.read_contacts.v1", EveSsoScopeCategory.ALLIANCES),
    ;

    companion object {
        private val byApiValue: Map<String, EveSsoScope> =
            entries.associateBy { it.apiValue }

        fun fromApiValue(apiValue: String): EveSsoScope? = byApiValue[apiValue]

        /** Known scopes from a grant list; unknown wire values are dropped. */
        fun parseGranted(apiValues: Collection<String>): Set<EveSsoScope> =
            apiValues.mapNotNullTo(linkedSetOf()) { fromApiValue(it) }

        /** Feature-gate helpers for scope-gated UI (kept for upcoming screens). */
        @Suppress("unused")
        fun inCategory(
            granted: Set<EveSsoScope>,
            category: EveSsoScopeCategory,
        ): Set<EveSsoScope> = granted.filterTo(linkedSetOf()) { it.category == category }

        @Suppress("unused")
        fun contains(granted: Set<EveSsoScope>, scope: EveSsoScope): Boolean =
            scope in granted

        @Suppress("unused")
        fun containsAny(granted: Set<EveSsoScope>, vararg scopes: EveSsoScope): Boolean =
            scopes.any { it in granted }

        @Suppress("unused")
        fun containsAll(granted: Set<EveSsoScope>, vararg scopes: EveSsoScope): Boolean =
            scopes.all { it in granted }

        @Suppress("unused")
        fun containsCategory(granted: Set<EveSsoScope>, category: EveSsoScopeCategory): Boolean =
            granted.any { it.category == category }
    }
}

/** UI / feature grouping for [EveSsoScope]. */
enum class EveSsoScopeCategory {
    PUBLIC,
    CALENDAR,
    LOCATION,
    MAIL,
    SKILLS,
    WALLET,
    SEARCH,
    CLONES,
    CHARACTERS,
    UNIVERSE,
    KILLMAILS,
    CORPORATIONS,
    ASSETS,
    PLANETS,
    FLEETS,
    UI,
    FITTINGS,
    MARKETS,
    INDUSTRY,
    CONTRACTS,
    ALLIANCES,
}
