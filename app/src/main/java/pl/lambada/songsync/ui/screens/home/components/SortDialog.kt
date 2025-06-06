package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.domain.model.SortOrders
import pl.lambada.songsync.domain.model.SortValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDialog(
    userSettingsController: UserSettingsController,
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
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.sort),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, bottom = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 6.dp))
                SortByRadioGroup(userSettingsController, onSortByChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp))
                SortOrderRadioGroup(userSettingsController, onSortOrderChange)
                HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
fun SortOrderRadioGroup(
    userSettingsController: UserSettingsController,
    onSortOrderChange: (SortOrders) -> Unit
) {
    Column {
        SortOrders.entries.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { onSortOrderChange(it) }
                    )
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = userSettingsController.sortOrder == it,
                    onClick = {
                        onSortOrderChange(it)
                    }
                )
                Text(stringResource(it.displayName))
            }
        }
    }
}

@Composable
fun SortByRadioGroup(
    userSettingsController: UserSettingsController,
    onSortByChange: (SortValues) -> Unit
) {
    Column {
        SortValues.entries.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onSortByChange(it) })
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = userSettingsController.sortBy == it,
                    onClick = {
                        onSortByChange(it)
                    }
                )
                Text(stringResource(it.displayName))
            }
        }
    }
}
