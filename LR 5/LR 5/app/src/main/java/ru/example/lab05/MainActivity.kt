package ru.example.lab05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold { insets ->
                    LabScreen(Modifier.padding(insets))
                }
            }
        }
    }
}

@Composable
fun LabScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var a by rememberSaveable { mutableStateOf("") }
    var b by rememberSaveable { mutableStateOf("") }
    var opName by rememberSaveable {
        mutableStateOf(Operation.ADD.name)
    }
    var resultText by rememberSaveable { mutableStateOf("") }
    var hasError by rememberSaveable { mutableStateOf(false) }

    val historySaver = listSaver<List<String>, String>(
        save = { it }, restore = { it }
    )
    var history by rememberSaveable(stateSaver = historySaver) {
        mutableStateOf(emptyList<String>())
    }

    val op = Operation.valueOf(opName)

    fun resetResult() {
        resultText = ""
        hasError = false
    }

    fun runCalculation() {
        val answer = calculate(a, b, op)
        val value = answer.value
        hasError = answer.error != null
        resultText = if (value != null) {
            val formatted = formatNumber(value)
            val entry = "${a.trim()} ${op.symbol} ${b.trim()} = $formatted"
            history = (history + entry).takeLast(5)
            context.getString(R.string.result_value, formatted)
        } else {
            val message = when (answer.error) {
                CalcError.DIVISION_BY_ZERO -> R.string.error_zero
                CalcError.OVERFLOW -> R.string.error_overflow
                else -> R.string.error_input
            }
            context.getString(message)
        }
    }

    Column(
        modifier.fillMaxSize().imePadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.screen_title),
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = a, onValueChange = { a = it; resetResult() },
            label = { Text(stringResource(R.string.number_a)) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = b, onValueChange = { b = it; resetResult() },
            label = { Text(stringResource(R.string.number_b)) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Text(stringResource(R.string.operation))
        OperationButtons(op) { selected ->
            opName = selected.name
            resetResult()
        }
        Button(
            onClick = { runCalculation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.calculate))
        }
        OutlinedButton(
            onClick = {
                a = ""; b = ""; resetResult()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_inputs))
        }
        AnimatedVisibility(visible = resultText.isNotEmpty()) {
            Text(
                resultText,
                style = MaterialTheme.typography.titleMedium,
                color = if (hasError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
        }
        if (history.isNotEmpty()) {
            HorizontalDivider()
            Text(
                stringResource(R.string.history),
                style = MaterialTheme.typography.titleMedium
            )
            history.forEach { entry -> Text(entry) }
            TextButton(onClick = { history = emptyList() }) {
                Text(stringResource(R.string.clear_history))
            }
        }
    }
}

@Composable
fun OperationButtons(
    selected: Operation,
    onSelect: (Operation) -> Unit
) {
    Operation.entries.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { operation ->
                FilterChip(
                    selected = selected == operation,
                    onClick = { onSelect(operation) },
                    label = { Text(operation.symbol) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}