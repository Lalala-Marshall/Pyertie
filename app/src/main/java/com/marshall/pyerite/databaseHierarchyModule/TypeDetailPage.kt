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
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseColumn
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TypeDetailPage(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
    iconManager: IconManager = koinInject()
) {
    // Fix flickering by remembering the flow
    val type by remember(typeId) { viewModel.typeDetail(typeId) }.collectAsState(initial = null)

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

            TypeSummaryCard(entity, iconManager)

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

            // Structure Section (Placeholder or fix if data found)
            BaseContainer(
                title = stringResource(R.string.structure),
                useSystemBarsPadding = false
            ) {
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        label = stringResource(R.string.structure_value),
                        value = "10 HP" // Placeholder
                    ),
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypeSummaryCard(entity: TypeEntity, iconManager: IconManager) {
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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = colorResource(R.color.border)
            )

            Text(
                text = entity.description.orEmpty(),
                fontSize = 14.sp,
                color = colorResource(R.color.text_primary),
                lineHeight = 20.sp
            )
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
                label = "体积", 
                value = "${formatter.format(entity.volume ?: 0.0)} m3"
            ))
            
            if (entity.repackagedVolume != null) {
                add(BaseDetailRowModel(
                    label = "体积(打包后)", 
                    value = "${formatter.format(entity.repackagedVolume)} m3"
                ))
            }
            
            if (entity.capacity != null && entity.capacity > 0) {
                add(BaseDetailRowModel(
                    label = "容量", 
                    value = "${formatter.format(entity.capacity)} m3"
                ))
            }
            
            if (entity.mass != null && entity.mass > 0) {
                add(BaseDetailRowModel(
                    label = "质量", 
                    value = "${formatter.format(entity.mass)} Kg"
                ))
            }
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
