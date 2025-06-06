package com.promptmaster.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.promptmaster.data.Prompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.TextSnippet // Import TextSnippet icon
import androidx.compose.material3.*

import androidx.compose.ui.Alignment

// Reusable PromptItem Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptItem(
    prompt: Prompt,
    onPromptClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onCopyDescriptionClick: (String) -> Unit // Add new parameter for copying description
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onPromptClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = prompt.title, style = MaterialTheme.typography.titleMedium)
            Text(text = prompt.description, style = MaterialTheme.typography.bodyMedium)
            // Add more UI elements for tags, model, etc. as needed

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite Icon
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (prompt.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (prompt.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = MaterialTheme.colorScheme.onSurface // Explicitly set tint color
                    )
                }

                // Copy Description Icon
                IconButton(onClick = { onCopyDescriptionClick(prompt.description) }) { // Use new parameter and pass description
                    Icon(
                        imageVector = Icons.Filled.TextSnippet, // Use TextSnippet icon
                        contentDescription = "Copy Description",
                        tint = MaterialTheme.colorScheme.onSurface // Explicitly set tint color
                    )
                }

                // Duplicate Icon
                IconButton(onClick = onDuplicateClick) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy, // Use ContentCopy icon
                        contentDescription = "Duplicate Prompt",
                        tint = MaterialTheme.colorScheme.onSurface // Explicitly set tint color
                    )
                }

                // Delete Icon
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Prompt",
                        tint = MaterialTheme.colorScheme.onSurface // Explicitly set tint color
                    )
                }
            }
        }
    }
}
