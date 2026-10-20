package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterType
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark2
import com.example.ui.theme.UltraCyan
import com.example.ui.theme.UltraPurple

@Composable
fun FilterPanel(
    currentFilter: FilterType,
    isBengali: Boolean,
    onSelectFilter: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = UltraCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBengali) "✨ ফিল্টার ও ইফেক্টস" else "✨ Filters & Effects",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = if (isBengali) "ভিডিওতে রিয়েল-টাইম কালার গ্রেডিং প্রয়োগ করুন" else "Apply instant cinematic color grading",
            fontSize = 11.sp,
            color = Color(0xFF8888A0),
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(FilterType.entries) { filter ->
                val isSelected = filter == currentFilter
                FilterCard(
                    filter = filter,
                    isSelected = isSelected,
                    isBengali = isBengali,
                    onClick = { onSelectFilter(filter) }
                )
            }
        }
    }
}

@Composable
fun FilterCard(
    filter: FilterType,
    isSelected: Boolean,
    isBengali: Boolean,
    onClick: () -> Unit
) {
    val gradientColors = when (filter) {
        FilterType.NORMAL -> listOf(Color(0xFF2A2A38), Color(0xFF1E1E28))
        FilterType.BW -> listOf(Color(0xFF666666), Color(0xFF222222))
        FilterType.SEPIA -> listOf(Color(0xFFD4A373), Color(0xFF704214))
        FilterType.HIGH_CONTRAST -> listOf(Color(0xFFFFFFFF), Color(0xFF000000))
        FilterType.CYBERPUNK -> listOf(Color(0xFF7C3AED), Color(0xFF00F5FF))
        FilterType.WARM -> listOf(Color(0xFFF59E0B), Color(0xFFDC2626))
        FilterType.COOL -> listOf(Color(0xFF0284C7), Color(0xFF1E1B4B))
        FilterType.INVERT -> listOf(Color(0xFF00FFFF), Color(0xFFFF007F))
        FilterType.VIBRANT -> listOf(Color(0xFFEC4899), Color(0xFFF97316))
    }

    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF1C1A2E) else CardDark2,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) UltraCyan else CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("filter_card_${filter.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Visual Swatch Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(18.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = UltraCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isBengali) filter.titleBn else filter.titleEn,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) UltraCyan else Color.White,
                maxLines = 1
            )
        }
    }
}
