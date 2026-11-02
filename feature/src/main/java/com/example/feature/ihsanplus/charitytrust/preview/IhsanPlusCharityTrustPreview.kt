package com.example.feature.ihsanplus.charitytrust.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.designsystem.theme.IhsanTheme
@Preview(showBackground = true)
@Composable
fun IhsanPlusCharityTrustPreview() {
    IhsanTheme {
        // Preview shell only. Demo dashboard data lives in DemoIhsanPlusCharityTrustDataSource
        // and must not be wired through production navigation or release DI.
    }
}
