package com.shk.smarty.ui.screens.modules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shk.smarty.model.Topic
import com.shk.smarty.ui.components.ErrorMessage
import com.shk.smarty.ui.components.LoadingIndicator
import com.shk.smarty.ui.components.SmartyScaffold
import com.shk.smarty.viewmodel.ModuleViewModel
import com.shk.smarty.di.ModuleViewModelFactoryProvider
import dagger.hilt.android.EntryPointAccessors

@Composable
fun ModuleScreen(
    subjectId: String,
    subjectTitle: String,
    onModuleClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    // Get the ModuleViewModel using Hilt EntryPoint
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        ModuleViewModelFactoryProvider::class.java
    )
    val factory = entryPoint.moduleViewModelFactory()
    val viewModel = remember(subjectId) {
        factory.create(subjectId)
    }
    
    val topics by viewModel.topics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    SmartyScaffold(
        title = subjectTitle,
        showBackButton = true,
        onBackClick = onBackClick
    ) { paddingModifier ->
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(message = error ?: "Unknown error occurred")
            topics.isEmpty() -> EmptyTopicList()
            else -> TopicList(
                topics = topics,
                modifier = paddingModifier,
                onTopicClick = onModuleClick
            )
        }
    }
}

@Composable
fun TopicList(
    topics: List<Topic>,
    modifier: Modifier = Modifier,
    onTopicClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(topics) { topic ->
            TopicItem(
                topic = topic,
                onTopicClick = onTopicClick
            )
        }
    }
}

@Composable
fun TopicItem(
    topic: Topic,
    onTopicClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTopicClick(topic.id, topic.name) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = topic.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = topic.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun EmptyTopicList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "No topics found",
            style = MaterialTheme.typography.titleMedium
        )
        
        Text(
            text = "Topics will appear here once they are added",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}