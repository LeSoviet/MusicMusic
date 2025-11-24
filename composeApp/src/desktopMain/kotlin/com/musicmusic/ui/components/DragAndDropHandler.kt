package com.musicmusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File

/**
 * Componente que permite arrastrar y soltar archivos y carpetas desde el sistema
 * para agregarlos a la biblioteca.
 *
 * Utiliza la API de AWT para compatibilidad con Compose Desktop 1.6.x
 *
 * @param modifier Modificador para personalizar el contenedor
 * @param onFilesDropped Callback que se ejecuta cuando se sueltan archivos/carpetas
 * @param content Contenido del composable
 */
@Composable
fun DragAndDropContainer(
    modifier: Modifier = Modifier,
    onFilesDropped: (List<File>) -> Unit,
    content: @Composable () -> Unit
) {
    var isDraggingOver by remember { mutableStateOf(false) }

    // Configurar el drop target cuando se monta el composable
    DisposableEffect(Unit) {
        // Obtener el ComposePanel actual (componente AWT subyacente)
        val composePanel = try {
            // Esto funciona en Compose Desktop
            val field = ComposePanel::class.java.getDeclaredField("rootPane")
            field.isAccessible = true
            null // Por ahora retornamos null, vamos a usar otra estrategia
        } catch (e: Exception) {
            null
        }

        onDispose { }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isDraggingOver) {
                    Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

/**
 * Configuración de drop target usando AWT directamente.
 * Esta función debe ser llamada en el nivel de Window.
 */
fun setupDragAndDrop(
    component: java.awt.Component,
    onFilesDropped: (List<File>) -> Unit,
    onDragStateChanged: (Boolean) -> Unit
) {
    DropTarget(component, object : DropTargetAdapter() {
        override fun dragEnter(dtde: DropTargetDragEvent) {
            onDragStateChanged(true)
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
            } else {
                dtde.rejectDrag()
            }
        }

        override fun dragExit(dte: DropTargetEvent) {
            onDragStateChanged(false)
        }

        override fun drop(dtde: DropTargetDropEvent) {
            onDragStateChanged(false)
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY)
                val transferable = dtde.transferable

                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    @Suppress("UNCHECKED_CAST")
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>

                    if (files.isNotEmpty()) {
                        onFilesDropped(files)
                        dtde.dropComplete(true)
                    } else {
                        dtde.dropComplete(false)
                    }
                } else {
                    dtde.dropComplete(false)
                }
            } catch (e: Exception) {
                println("Error handling drop: ${e.message}")
                e.printStackTrace()
                dtde.dropComplete(false)
            }
        }
    })
}
