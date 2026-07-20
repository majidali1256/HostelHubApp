package com.hostelhub.ui.agreements

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hostelhub.ui.theme.Primary
import com.hostelhub.ui.theme.Success
import java.io.ByteArrayOutputStream

data class SignatureStroke(
    val points: MutableList<Offset> = mutableListOf(),
    val color: Color = Color(0xFF1E293B),
    val strokeWidth: Float = 7f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ESignatureCanvasModal(
    agreementId: String,
    agreementTitle: String,
    onDismiss: () -> Unit,
    onConfirmSignature: (String) -> Unit
) {
    val strokes = remember { mutableStateListOf<SignatureStroke>() }
    var currentStroke by remember { mutableStateOf<SignatureStroke?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Draw, contentDescription = null, tint = Primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Digital E-Signature",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ID: #${agreementId.takeLast(6)} • $agreementTitle",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please draw your physical signature inside the box using your finger or stylus. Your signature constitutes legally binding agreement to all tenancy terms.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(
                            width = 2.dp,
                            color = if (errorMessage != null) MaterialTheme.colorScheme.error else Primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    errorMessage = null
                                    val newStroke = SignatureStroke(mutableListOf(offset))
                                    currentStroke = newStroke
                                    strokes.add(newStroke)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStroke?.points?.add(change.position)
                                },
                                onDragEnd = {
                                    currentStroke = null
                                },
                                onDragCancel = {
                                    currentStroke = null
                                }
                            )
                        }
                ) {
                    // Guide Line & Watermark
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "X",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Divider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.5.dp,
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TOUCHSCREEN SIGNATURE CANVAS • LEGALLY BINDING E-SIGNATURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Drawing Strokes
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        strokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                val path = Path()
                                path.moveTo(stroke.points.first().x, stroke.points.first().y)
                                for (i in 1 until stroke.points.size) {
                                    val point = stroke.points[i]
                                    path.lineTo(point.x, point.y)
                                }
                                drawPath(
                                    path = path,
                                    color = stroke.color,
                                    style = Stroke(
                                        width = stroke.strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            } else if (stroke.points.size == 1) {
                                val point = stroke.points.first()
                                drawCircle(
                                    color = stroke.color,
                                    radius = stroke.strokeWidth / 2f,
                                    center = point
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Canvas Actions (Clear & Undo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { strokes.clear() },
                        enabled = strokes.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Canvas", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                strokes.removeAt(strokes.lastIndex)
                            }
                        },
                        enabled = strokes.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Undo Stroke", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Signature Button
                Button(
                    onClick = {
                        if (strokes.isEmpty() || strokes.sumOf { it.points.size } < 5) {
                            errorMessage = "Please draw your full signature before confirming."
                        } else {
                            // Generate simulated Base64 signature data representation
                            val strokeCount = strokes.size
                            val pointCount = strokes.sumOf { it.points.size }
                            val base64Token = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAZAAAADICAYAAADGFbfiAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAALEgAACxIB0t1+/AAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNui8sowAAAAWdEVYdENyZWF0aW9uIFRpbWUAMDgvMTIvMDhjS8/KAAAAsklEQVR4nO3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA+A81gAAG/1s/uAAAAAElFTkSuQmCC_sig_${System.currentTimeMillis()}_s${strokeCount}_p${pointCount}"
                            onConfirmSignature(base64Token)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Digital Signature",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
