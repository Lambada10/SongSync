package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.util.ext.BackPressHandler

@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    showSearch: Boolean,
    onShowSearchChange: (Boolean) -> Unit,
    showingSearch: Boolean,
    onShowingSearchChange: (Boolean) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var willShowIme by remember { mutableStateOf(false) }

    @OptIn(ExperimentalLayoutApi::class)
    val showingIme = WindowInsets.isImeVisible

    if (!showingSearch && showSearch) {
        onShowingSearchChange(true)
    }

    if (!showSearch && !willShowIme && showingSearch && !showingIme && query.isEmpty()) {
        onShowingSearchChange(false)
    }

    if (willShowIme && showingIme) {
        willShowIme = false
    }
    val focusManager = LocalFocusManager.current

    BackPressHandler(
        enabled = showingSearch,
        onBackPressed = {
            onQueryChange("")
            onShowSearchChange(false)
            onShowingSearchChange(false)
        }
    )

    TextField(
        query,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search),
                modifier = Modifier.clickable {
                    onShowSearchChange(false)
                    onShowingSearchChange(false)
                }
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(id = R.string.clear),
                modifier = Modifier.clickable {
                    onQueryChange("")
                    onShowSearchChange(false)
                    onShowingSearchChange(false)
                }
            )
        },
        placeholder = { Text(stringResource(id = R.string.search)) },
        shape = ShapeDefaults.ExtraLarge,
        colors = TextFieldDefaults.colors(
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .padding(end = 18.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused && !showingIme) willShowIme = true
            }
            .onGloballyPositioned {
                if (showSearch && !showingIme) {
                    focusRequester.requestFocus()
                    onShowSearchChange(false)
                }
            },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
        )
    )
}