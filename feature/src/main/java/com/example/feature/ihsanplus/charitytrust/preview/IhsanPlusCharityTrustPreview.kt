package com.example.feature.ihsanplus.charitytrust.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.charitytrust.data.IhsanPlusCharityTrustFakeDataSource
import com.example.feature.ihsanplus.charitytrust.presentation.screen.IhsanPlusCharityTrustScreen
import com.example.feature.ihsanplus.charitytrust.presentation.state.IhsanPlusCharityTrustUiState

@Preview(showBackground = true)
@Composable
fun IhsanPlusCharityTrustPreview() {
    IhsanTheme {
        // We can't easily use the real ViewModel in preview without Koin setup
        // So we just show the screen shell which will default to loading if not provided
        // or we could refactor the screen to accept state directly (recommended)
    }
}
