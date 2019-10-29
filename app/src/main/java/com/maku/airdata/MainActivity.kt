package com.maku.airdata

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.maku.airdata.databinding.ActivityMainBinding

import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import com.bumptech.glide.Glide
import android.R.attr.bitmap
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.firebase.ml.vision.FirebaseVision


class MainActivity : AppCompatActivity() {

    //This will identify your intent when it returns
    private val TAKE_PHOTO_REQUEST_CODE = 1

    //tracks wether you have taken a photo
    private var pictureTaken: Boolean = false

    //glue between the layout and its views
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        //remove load button from screen
        binding.cm.loadAitimeBtn.visibility = View.GONE

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            takePictureWithCamera()
        }
    }

    private fun takePictureWithCamera() {
        // 1
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        //start an activity to perfom the action
        startActivityForResult(captureIntent, TAKE_PHOTO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TAKE_PHOTO_REQUEST_CODE
            && resultCode == RESULT_OK) {
            setImageViewWithImage(data)
        }
    }

    private fun setImageViewWithImage(data: Intent?) {

        val thumbnail = data!!.extras!!.get("data") as Bitmap
        binding.cm.imageView.setImageBitmap(thumbnail)

        //hide textview
        binding.cm.instruction.visibility = View.GONE

        pictureTaken = true

        //hide floating action button
        binding.fab.isVisible = false
        //add load button to screen
        binding.cm.loadAitimeBtn.visibility = View.VISIBLE

        firebaseMlKit(thumbnail)
    }

    private fun firebaseMlKit(thumbnail: Bitmap) {

        //Create a FirebaseVisionImage object from your image.
        val image = FirebaseVisionImage.fromBitmap(thumbnail)

        // [START get_detector_default]
        val detector = FirebaseVision.getInstance()
            .onDeviceTextRecognizer
        // [END get_detector_default]

        // [START run_detector]
        val result = detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                // Task completed successfully
                // [START_EXCLUDE]
                // [START get_text]
                for (block in firebaseVisionText.textBlocks) {
                    val boundingBox = block.boundingBox
                    val cornerPoints = block.cornerPoints
                    val text = block.text

                    for (line in block.lines) {
                        // ...
                        for (element in line.elements) {
                            // ...
                            binding.cm.instruction.setText(element.text)

                            //show textview
                            binding.cm.instruction.visibility = View.VISIBLE
                            //show floating action button
                            binding.fab.isVisible = true

                            binding.cm.loadAitimeBtn.setOnClickListener {

                                // Creating our Share Intent
                                val shareIntent = Intent()
                                shareIntent.action = Intent.ACTION_SEND
                                shareIntent.type="text/plain"
                                shareIntent.putExtra(Intent.EXTRA_TEXT, element.text);
                                startActivity(Intent.createChooser(shareIntent,getString(R.string.send_to)))

                            }
                        }
                    }
                }
                // [END get_text]
                // [END_EXCLUDE]
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                e.stackTrace
            }
        // [END run_detector]

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
