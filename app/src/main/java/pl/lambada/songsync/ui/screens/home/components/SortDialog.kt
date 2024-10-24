package pl.lambada.songsync.ui.screens.home.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.SortOrders
import pl.lambada.songsync.domain.model.SortValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDialog(
    sortBy: SortValues,
    sortOrder: SortOrders,
    onDismiss: () -> Unit,
    onSortByChange: (SortValues) -> Unit,
    onSortOrderChange: (SortOrders) -> Unit
) {
    BasicAlertDialog(onDismiss) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column {
                Text(
                    text = stringResource(R.string.sort),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, bottom = 16.dp)
                )
                SortByRadioGroup(sortBy, onSortByChange)
                HorizontalDivider()
                SortOrderRadioGroup(sortOrder, onSortOrderChange)
                Row(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@Composable
fun SortOrderRadioGroup(sortOrder: SortOrders, onSortOrderChange: (SortOrders) -> Unit) {
    var selected by remember { mutableStateOf(sortOrder) }
    Column {
        SortOrders.entries.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selected == it,
                    onClick = {
                        selected = it
                        onSortOrderChange(it)
                    }
                )
                Text(
                    stringResource(it.displayName),
                    modifier = Modifier.clickable {
                        selected = it
                        onSortOrderChange(it)
                    }
                )
            }
        }
    }
}

@Composable
fun SortByRadioGroup(sortBy: SortValues, onSortByChange: (SortValues) -> Unit) {
    var selected by remember { mutableStateOf(sortBy) }
    Column {
        SortValues.entries.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selected == it,
                    onClick = {
                        selected = it
                        onSortByChange(it)
                    }
                )
                Text(
                    stringResource(it.displayName),
                    modifier = Modifier.clickable {
                        selected = it
                        onSortByChange(it)
                    }
                )
            }
        }
    }
}
