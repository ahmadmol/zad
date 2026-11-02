package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun HomeEhsanCommunityCard(onNavigateToDonations: () -> Unit, modifier: Modifier = Modifier,
    offersCount: Int = 0, requestsCount: Int = 0) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = IhsanTheme.colors.surfaceMint) {
        Row(Modifier.padding(10.dp).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.correction_community_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.polish_community_subtitle), style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onNavigateToDonations, contentPadding = PaddingValues(4.dp), modifier = Modifier.weight(1f), colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                        Icon(Icons.Outlined.CardGiftcard, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(stringResource(R.string.home_ehsan_offers_label), style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onNavigateToDonations, contentPadding = PaddingValues(4.dp), modifier = Modifier.weight(1f), colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                        Icon(Icons.Outlined.VolunteerActivism, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(stringResource(R.string.home_ehsan_requests_label), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Image(painterResource(R.drawable.ihsan_charity_growth), null, contentScale = ContentScale.Crop,
                modifier = Modifier.width(94.dp).height(86.dp).clip(RoundedCornerShape(14.dp)))
        }
    }
}
