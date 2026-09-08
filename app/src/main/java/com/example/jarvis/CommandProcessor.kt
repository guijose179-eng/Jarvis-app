package com.example.jarvis

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings

class CommandProcessor(private val context: Context, private val onReply: (String) -> Unit) {

    fun process(command: String) {
        val c = command.lowercase()

        when {
            c.startsWith("ligar para") || c.startsWith("ligue para") -> {
                val name = c.substringAfter("para").trim()
                callContact(name)
            }

            c.startsWith("mandar mensagem para") || c.startsWith("mande mensagem para") -> {
                val rest = c.substringAfter("para").trim()
                onReply("Certo, o que devo dizer para $rest?")
                openApp("com.whatsapp")
            }

            c.contains("ligar wifi") || c.contains("ativar wifi") -> {
                context.startActivity(Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                onReply("Abrindo painel de Wi-Fi.")
            }

            c.contains("ligar bluetooth") -> {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                onReply("Abrindo Bluetooth.")
            }

            c.contains("ligar lanterna") || c.contains("acender lanterna") -> {
                setFlashlight(true); onReply("Lanterna ligada.")
            }
            c.contains("desligar lanterna") || c.contains("apagar lanterna") -> {
                setFlashlight(false); onReply("Lanterna apagada.")
            }

            c.contains("aumentar volume") -> { adjustVolume(true); onReply("Volume aumentado.") }
            c.contains("diminuir volume") -> { adjustVolume(false); onReply("Volume reduzido.") }
            c.contains("modo silencioso") || c.contains("silenciar") -> {
                setSilent(); onReply("Modo silencioso ativado.")
            }

            c.startsWith("criar alarme") || c.startsWith("definir alarme") -> {
                onReply("Abrindo o relógio para configurar o alarme.")
                context.startActivity(Intent(AlarmClock.ACTION_SET_ALARM).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            c.startsWith("abrir") || c.startsWith("abra") -> {
                val appName = c.substringAfter("abrir").substringAfter("abra").trim()
                onReply("Abrindo $appName.")
                JarvisAccessibilityService.instance?.openAppByName(appName)
                    ?: onReply("Preciso que a Acessibilidade do Jarvis esteja ativada nas configurações para isso.")
            }

            c.startsWith("pesquisar") || c.startsWith("pesquise") -> {
                val query = c.substringAfter("pesquisar").substringAfter("pesquise").trim()
                webSearch(query)
                onReply("Pesquisando $query.")
            }

            c.contains("abrir configurações") -> {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                onReply("Abrindo configurações.")
            }

            else -> {
                onReply("Não reconheci esse comando ainda, senhor. Pode repetir de outro jeito?")
            }
        }
    }

    private fun callContact(name: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$name"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun openApp(packageName: String) {
        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }

    private fun setFlashlight(on: Boolean) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = cameraManager.cameraIdList.firstOrNull() ?: return
        cameraManager.setTorchMode(id, on)
    }

    private fun adjustVolume(up: Boolean) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun setSilent() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    private fun webSearch(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
