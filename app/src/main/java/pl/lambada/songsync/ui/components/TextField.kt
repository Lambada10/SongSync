package pl.lambada.songsync.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun CommonTextField(
    value: String = "",
    onValueChange: (Any?) -> Unit = {},
    label: String = "",
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth().padding(8.dp),
    ) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = singleLine,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        readOnly = readOnly,
        modifier = modifier,
    )
}
