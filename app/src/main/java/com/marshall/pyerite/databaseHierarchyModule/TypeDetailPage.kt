package com.marshall.pyerite.databaseHierarchyModule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.*
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.NumberFormat
import java.util.Locale

private val DOGMA_CATEGORY_WEIGHTS = mapOf(
    1 to 10,   // Fitting
    2 to 20,   // Shield
    3 to 21,   // Armor
    4 to 22,   // Structure
    5 to 30,   // Capacitor
    17 to 40,  // Navigation
    6 to 50,   // Targeting
    37 to 60,  // Bonuses
    20 to 70,  // Remote Support
    10 to 80,  // Drones
    34 to 81,  // Fighter abilities
    38 to 82,  // Fighter attributes
    40 to 90,  // Hangars & Bays
    51 to 100, // Mining
    36 to 110, // EWar Resistances
    39 to 120, // Superweapons
    7 to 200,  // Misc
    19 to 210, // Loot
    41 to 220, // Ship Death
    52 to 230, // Other
    8 to 150,  // Skills
    9 to 240,  // Uncategorized
)

private val DOGMA_CATEGORY_NAMES = mapOf(
    1 to "装配",
    2 to "护盾",
    3 to "装甲",
    4 to "结构",
    5 to "电容器",
    6 to "锁定",
    7 to "杂项",
    8 to "技能需求",
    9 to "未分类",
    10 to "无人机",
    12 to "AI属性",
    17 to "导航",
    19 to "战利品",
    20 to "远程支援",
    34 to "铁骑舰载机能力",
    36 to "电子战防御",
    37 to "加成",
    38 to "铁骑舰载机属性",
    39 to "末日武器",
    40 to "挂载 & 舱室",
    41 to "死亡/残骸",
    42 to "NPC行为",
    51 to "采矿",
    52 to "其他"
)

@Composable
fun TypeDetailPage(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel()
) {
    // Fix flickering by remembering the flow
    val type by remember(typeId) { viewModel.typeDetail(typeId) }.collectAsState(initial = null)
    val attributes by remember(typeId) { viewModel.typeAttributes(typeId) }.collectAsState(initial = emptyList())

    type?.let { entity ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.type_info),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(start = 24.dp, bottom = 12.dp, top = 12.dp)
            )

            TypeSummaryCard(entity)

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Item for Market
            BaseContainer(useSystemBarsPadding = false) {
                BaseColumn(
                    items = listOf(
                        BaseLazyColumnItemModel(
                            iconRes = R.drawable.ic_database, // Should be market icon
                            itemName = stringResource(R.string.region_market),
                            onClick = { /* TODO */ }
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            BaseInfoSection(entity)

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Dogma Sections
            DynamicDogmaSections(attributes)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DynamicDogmaSections(attributes: List<TypeAttributeDetail>) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    
    // Core attributes to skip in dynamic sections (already in Base Info or Summary)
    // ID 4: Mass, 38: Capacity, 161: Volume, 422: Tech Level
    val coreAttributeIds = setOf(4, 38, 161, 422) 
    
    val groupedAttributes = remember(attributes) {
        attributes
            .filter { attr ->
                attr.attributeId !in coreAttributeIds && 
                attr.displayName != null && 
                (attr.value != null && attr.value != attr.defaultValue)
            }
            .groupBy { it.categoryId }
            .toList()
            .sortedBy { (categoryId, _) -> 
                DOGMA_CATEGORY_WEIGHTS[categoryId] ?: when (categoryId) {
                    0 -> 250
                    in setOf(12, 42), in 21..33 -> 800 // NPC stuff
                    else -> 500 // Unknown categories in middle
                }
            }
    }

    groupedAttributes.forEach { (categoryId, attrList) ->
        val categoryName = DOGMA_CATEGORY_NAMES[categoryId] ?: attrList.firstOrNull()?.categoryName ?: "其他"
        BaseContainer(
            title = categoryName,
            useSystemBarsPadding = false,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Column {
                attrList.forEachIndexed { index, attr ->
                    BaseDetailRow(
                        model = BaseDetailRowModel(
                            iconFileName = attr.iconFilename,
                            label = attr.displayName ?: attr.name ?: "未知属性",
                            value = "${formatter.format(attr.value ?: attr.defaultValue ?: 0.0)} ${attr.unitName ?: ""}"
                        ),
                        showDivider = index != attrList.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeSummaryCard(entity: TypeEntity, iconManager: IconManager = koinInject()) {
    BaseContainer(useSystemBarsPadding = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    painter = entity.iconFilename?.let { 
                        rememberAsyncImagePainter(iconManager.getIconFile(it))
                    } ?: painterResource(R.drawable.ic_database),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = entity.zhName ?: entity.name.orEmpty(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_primary)
                    )
                    Text(
                        text = "${entity.categoryName ?: ""} / ${entity.groupName ?: ""} / ID:${entity.id}",
                        fontSize = 12.sp,
                        color = colorResource(R.color.hint_text)
                    )
                }
            }

            if (entity.description?.isNotEmpty() == true) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = colorResource(R.color.border)
                )

                Text(
                    text = entity.description,
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_primary),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun BaseInfoSection(entity: TypeEntity) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    // Logic for conditional visibility of detail rows
    val detailItems = remember(entity) {
        buildList {
            // Volume is ALWAYS shown
            add(BaseDetailRowModel(
                iconFileName = "attribute_67.png",
                label = "体积", 
                value = "${formatter.format(entity.volume ?: 0.0)} m3"
            ))
            
            if (entity.repackagedVolume != null) {
                add(BaseDetailRowModel(
                    iconFileName = "attribute_67.png",
                    label = "体积(打包后)", 
                    value = "${formatter.format(entity.repackagedVolume)} m3"
                ))
            }
            
            if (entity.capacity != null && entity.capacity > 0) {
                add(BaseDetailRowModel(
                    iconFileName = "attribute_71.png",
                    label = "容量", 
                    value = "${formatter.format(entity.capacity)} m3"
                ))
            }
            
            if (entity.mass != null && entity.mass > 0) {
                add(BaseDetailRowModel(
                    iconFileName = "attribute_76.png",
                    label = "质量", 
                    value = "${formatter.format(entity.mass)} Kg"
                ))
            }

            // Tech Level - Attribute 422
            // Since tech level isn't in the entity columns but is a core concept
            // (Note: If we decide to add tech_level to TypeEntity later, we can use it here)
        }
    }

    if (detailItems.isNotEmpty()) {
        BaseContainer(
            title = stringResource(R.string.base_info),
            useSystemBarsPadding = false
        ) {
            Column {
                detailItems.forEachIndexed { index, itemModel ->
                    BaseDetailRow(
                        model = itemModel,
                        showDivider = index != detailItems.lastIndex
                    )
                }
            }
        }
    }
}
