package com.example.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.*

@Preview(name = "Emerald / Light", locale = "ar", showBackground = true)
@Preview(name = "Emerald / Dark", locale = "ar", uiMode = 0x20, showBackground = true)
@Preview(name = "Large type", locale = "ar", fontScale = 2f, showBackground = true)
@Composable
private fun PremiumComponentsPreview() { IhsanTheme { PreviewContent() } }

@Preview(name = "Sand", locale = "ar", showBackground = true)
@Composable
private fun SandPreview() { IhsanTheme(palette = IhsanPalette.SAND) { PreviewContent() } }

@Preview(name = "Midnight", locale = "ar", showBackground = true)
@Composable
private fun MidnightPreview() { IhsanTheme(darkTheme = true, palette = IhsanPalette.MIDNIGHT) { PreviewContent() } }

@Composable
private fun PreviewContent() {
    Surface {
        Column(Modifier.width(360.dp).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IhsanSectionHeader("مساحة ليومك", "خير صغير، كل يوم")
            IhsanIllustration(IhsanScene.AQSA, Modifier.fillMaxWidth().height(200.dp))
            IhsanServiceCard("اتجاه القبلة", Icons.Outlined.Explore, {})
            IhsanDailyGoalCard("قراءة القرآن", 2, 5, {})
            IhsanPrimaryButton({}) { Text("ابدأ الآن") }
        }
    }
}
