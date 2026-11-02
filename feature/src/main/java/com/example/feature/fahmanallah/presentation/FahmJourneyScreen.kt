package com.example.feature.fahmanallah.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.ihsanTextButtonColors
import com.example.feature.fahmanallah.presentation.components.FahmJourneyItem
import com.example.feature.fahmanallah.presentation.components.FahmProgressRing

@Composable
fun FahmJourneyScreen(viewModel: FahmViewModel,onNavigateToDetail: (String) -> Unit,onBackClick: () -> Unit) {
    val state by viewModel.mainState.collectAsStateWithLifecycle()
    val detail by viewModel.detailState.collectAsStateWithLifecycle()
    Scaffold(topBar={IhsanTopBar("رحلتي في الفهم عن الله",onNavigateBack=onBackClick)}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item {Text("تقدمك بتسجيلك اليدوي. رتّب الدروس كما يناسب رحلتك؛ يبقى محتواها ومحفوظاتك كما هما.")}
            if(!detail.isOnline) item {Text("أنت دون اتصال؛ التقدم والمحفوظات محليان، ويحتاج تشغيل الفيديو إلى اتصال.")}
            if(state.isLoading) item {IhsanLoadingState()}
            state.error?.let {message->item {IhsanErrorState("تعذر قراءة الرحلة",message=message,onRetry=viewModel::reload)}}
            state.actionError?.let {message->item {Text(message,color=MaterialTheme.colorScheme.error)}}
            if(!state.isLoading && state.error==null) {
                item {FahmProgressRing(progress=state.journeyProgress)}
                state.continueEpisode?.let {ep->item {
                    IhsanButton(onClick={onNavigateToDetail(ep.id)}) {Text("متابعة: ${ep.title}")}
                }}
                if(state.episodes.isEmpty()) item {Text("لا توجد دروس متاحة في المحتوى الحالي.")}
                itemsIndexed(state.episodes,key={_,ep->ep.id}) {index,episode->
                    IhsanContentCard {
                        FahmJourneyItem(episode=episode,onEpisodeClick=onNavigateToDetail)
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                            TextButton(colors=ihsanTextButtonColors(),enabled=index>0 && !state.busy,
                                onClick={viewModel.moveEpisode(episode.id,-1)}) {Text("تقديم الدرس")}
                            TextButton(colors=ihsanTextButtonColors(),enabled=index<state.episodes.lastIndex && !state.busy,
                                onClick={viewModel.moveEpisode(episode.id,1)}) {Text("تأخير الدرس")}
                        }
                    }
                }
            }
            item {Spacer(Modifier.navigationBarsPadding().height(16.dp))}
        }
    }
}
