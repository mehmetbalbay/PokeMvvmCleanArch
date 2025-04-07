package com.example.pokemonapp.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Tüm ekranı kaplayan hata gösterge ekranı
 */
@Composable
fun FullScreenError(
    message: String,
    onRetry: () -> Unit,
    retryButtonText: String = "Tekrar Dene",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ErrorContent(
            message = message,
            onRetry = onRetry,
            retryButtonText = retryButtonText,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

/**
 * Hata içeriği gösteren bileşen
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    retryButtonText: String = "Tekrar Dene",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(retryButtonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorContentPreview() {
    ErrorContent(
        message = "Bir hata oluştu. Lütfen daha sonra tekrar deneyin.",
        onRetry = {},
        retryButtonText = "Tekrar Dene"
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FullScreenErrorPreview() {
    FullScreenError(
        message = "Bir hata oluştu. Lütfen daha sonra tekrar deneyin.",
        onRetry = {}
    )
} 