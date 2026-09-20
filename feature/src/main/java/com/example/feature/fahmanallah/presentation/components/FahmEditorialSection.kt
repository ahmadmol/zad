package com.example.feature.fahmanallah.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme

@Composable
fun FahmRuleCard(
    ruleText: String,
    modifier: Modifier = Modifier,
    ruleTitle: String = "قاعدة اليوم"
) {
    val amberBg = Color(0xFFFFF8E1)
    val amberBorder = Color(0xFFFFE082)
    val amberIconColor = Color(0xFFF57F17)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (IhsanTheme.isDark) Color(0xFF2C2415) else amberBg
        ),
        border = BorderStroke(1.dp, if (IhsanTheme.isDark) Color(0xFF5D4825) else amberBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(amberIconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = amberIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = ruleTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = amberIconColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ruleText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = IhsanTheme.colors.textPrimary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun FahmUnderstandSection(
    understandText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "ماذا أفهم عن الله؟",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = understandText,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Normal,
            color = IhsanTheme.colors.textPrimary,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun FahmTakeawaysSection(
    takeaways: List<String>,
    modifier: Modifier = Modifier
) {
    if (takeaways.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "ماذا أتعلم؟",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        takeaways.forEach { takeaway ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = takeaway,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IhsanTheme.colors.textPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
