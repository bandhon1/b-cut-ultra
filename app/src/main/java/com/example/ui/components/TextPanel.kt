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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TextOverlay
import com.example.model.TextPosition
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark2
import com.example.ui.theme.UltraCyan
import com.example.ui.theme.UltraPink
import com.example.ui.theme.UltraPurple
import com.example.ui.theme.UltraPurpleLight

@Composable
fun TextPanel(
    activeOverlay: TextOverlay?,
    isBengali: Boolean,
    onApplyText: (String, TextPosition, Float, Long, Boolean, Boolean) -> Unit,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember(activeOverlay) { mutableStateOf(activeOverlay?.text ?: "B-CUT ULTRA | By Bandhon") }
    var selectedPosition by remember(activeOverlay) { mutableStateOf(activeOverlay?.position ?: TextPosition.BOTTOM) }
    var fontSize by remember(activeOverlay) { mutableFloatStateOf(activeOverlay?.fontSizeSp ?: 24f) }
    var selectedColorHex by remember(activeOverlay) { mutableLongStateOf(activeOverlay?.textColorHex ?: 0xFFFFFFFF) }
    var hasBackgroundBox by remember(activeOverlay) { mutableStateOf(activeOverlay?.hasBackgroundBox ?: true) }
    var hasDropShadow by remember(activeOverlay) { mutableStateOf(activeOverlay?.hasDropShadow ?: true) }

    val presetSnippets = listOf(
        "By Bandhon",
        "B-CUT ULTRA PRO",
        "🔥 VIRAL REEL",
        "SUBSCRIBE",
        "BREAKING NEWS"
    )

    val colorOptions = listOf(
        0xFFFFFFFF to "White",
        0xFF00F5FF to "Cyan",
        0xFFFF007F to "Pink",
        0xFFA855F7 to "Purple",
        0xFFFACC15 to "Yellow",
        0xFF10B981 to "Lime"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = UltraPink,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBengali) "🔤 টেক্সট ও ওয়াটারমার্ক" else "🔤 Text & Watermark",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Input Field
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = { Text(if (isBengali) "টেক্সট লিখুন..." else "Enter text / subtitle...", color = Color(0xFF6B6B85)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("text_overlay_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardDark2,
                unfocusedContainerColor = CardDark2,
                focusedBorderColor = UltraCyan,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Presets Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(presetSnippets) { preset ->
                Surface(
                    onClick = { textInput = preset },
                    color = CardDark2,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = preset,
                        fontSize = 10.sp,
                        color = UltraCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Position selector
        Text(
            text = if (isBengali) "পজিশন (Position):" else "Placement:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFA0A0BA)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextPosition.entries.forEach { pos ->
                val isSelected = selectedPosition == pos
                Surface(
                    onClick = { selectedPosition = pos },
                    color = if (isSelected) UltraPurple else CardDark2,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) UltraCyan else CardBorder
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isBengali) pos.labelBn else pos.labelEn,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFFB0B0C4),
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // Font Size Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBengali) "ফন্ট সাইজ:" else "Font Size:",
                fontSize = 11.sp,
                color = Color(0xFFA0A0BA)
            )
            Text(
                text = "${fontSize.toInt()} sp",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UltraCyan
            )
        }

        Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 14f..46f,
            colors = SliderDefaults.colors(
                thumbColor = UltraCyan,
                activeTrackColor = UltraPurple,
                inactiveTrackColor = CardBorder
            ),
            modifier = Modifier.height(20.dp)
        )

        // Color selection
        Text(
            text = if (isBengali) "টেক্সট কালার:" else "Text Color:",
            fontSize = 11.sp,
            color = Color(0xFFA0A0BA)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            colorOptions.forEach { (colorHex, _) ->
                val isSelected = selectedColorHex == colorHex
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(colorHex), CircleShape)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color.White else CardBorder,
                            shape = CircleShape
                        )
                        .clickable { selectedColorHex = colorHex }
                )
            }
        }

        // Toggles: Box background & Drop shadow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBengali) "ডার্ক ব্যাকগ্রাউন্ড বক্স" else "Background Backdrop Pill",
                fontSize = 11.sp,
                color = Color(0xFFD0D0E0)
            )
            Switch(
                checked = hasBackgroundBox,
                onCheckedChange = { hasBackgroundBox = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = UltraCyan,
                    checkedTrackColor = UltraPurple
                )
            )
        }

        // Apply & Clear Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onApplyText(
                        textInput,
                        selectedPosition,
                        fontSize,
                        selectedColorHex,
                        hasBackgroundBox,
                        hasDropShadow
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("apply_text_overlay_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = UltraPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isBengali) "ভিডিওতে টেক্সট বসাও" else "Apply Text",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onClearText,
                modifier = Modifier
                    .weight(0.7f)
                    .testTag("clear_text_overlay_btn"),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = if (isBengali) "মুছে ফেলো" else "Clear",
                    fontSize = 12.sp,
                    color = Color(0xFFE08888)
                )
            }
        }
    }
}
