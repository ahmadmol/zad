package com.example.feature.ihsanplus.daily.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.daily.presentation.screen.IhsanPlusDailyExperienceScreen

@Preview(showBackground = true)
@Composable
fun IhsanPlusDailyExperiencePreview() {
    IhsanTheme {
        IhsanPlusDailyExperienceScreen()
    }
}
