package com.example.myapplication.animation

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentOrderSuccessBinding
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class OrderSuccessFragment : Fragment() {

    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!

    private lateinit var konfettiView: KonfettiView
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)
        konfettiView = binding.konfettiView

        startConfetti()
        playSuccessSoundIfAllowed()

        // ✅ Safe delayed navigation
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }, 3000)

        return binding.root
    }

    private fun startConfetti() {
        val party = Party(
            speed = 10f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfff44336.toInt(), 0xff4caf50.toInt(), 0xff2196f3.toInt()),
            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(50),
            position = Position.Relative(0.5, 0.0),
            timeToLive = 2000L,
            shapes = listOf(Shape.Square, Shape.Circle),
        )

        konfettiView.start(party)
    }

    private fun playSuccessSoundIfAllowed() {
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.success_sound)
            mediaPlayer?.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}
