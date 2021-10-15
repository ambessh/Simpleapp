package com.example.simpleapp.view.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.simpleapp.R
import com.example.simpleapp.util.Constants
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity(),View.OnClickListener {

    lateinit var imageView: ImageView
    lateinit var camera: Button
    lateinit var gallery:Button
     var currentPhotoPath:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        imageView=findViewById(R.id.imageView)
        camera=findViewById(R.id.camera)
        gallery=findViewById(R.id.gallery)

        camera.setOnClickListener(this)
        gallery.setOnClickListener(this)

        supportActionBar?.setTitle("Home")
        populateIfAvailable()
    }

    fun populateIfAvailable(){
        val returnedImage= intent.getParcelableExtra<Uri>("returnedImage")
        if(returnedImage!=null){
            Glide.with(this).load(returnedImage).into(imageView)
             currentPhotoPath=returnedImage.toString()
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        return super.onCreateView(name, context, attrs)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menufirst,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.move->{
            moveToEditFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun moveToEditFragment(){
        Log.i("moving","moving")
        if(currentPhotoPath!=null) {
            val intent = Intent(this@HomeActivity, EditActivity::class.java)
            intent.putExtra("imagepath",currentPhotoPath)
            startActivity(intent)
        }else{
            Toast.makeText(this@HomeActivity,"No image selected!",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(v: View?) {
     if(v!=null){
          when(v.id){
              R.id.camera ->{
                  openCamera()
                  return
              }
              R.id.gallery ->{
                  openGallery()
                  return
              }
              else->{
                  return
              }
          }
        }
    }


    fun openCamera(){
       requestPermissionsCamera()
    }


    fun openGallery(){
     requestPermissionsGallery()
    }

    fun requestPermissionsCamera(){
        if(ContextCompat.checkSelfPermission(this@HomeActivity,android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@HomeActivity, arrayOf(Manifest.permission.CAMERA),Constants.CAMERA_CODE)
        }else{
            openCameraNow()
        }
    }

    fun requestPermissionsGallery(){
        if(ContextCompat.checkSelfPermission(this@HomeActivity,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@HomeActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),Constants.GALLERY_CODE)
        }else{
            openGalleryNow()
        }
    }

    fun openGalleryNow(){
      try{

          val intent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
          startActivityForResult(intent, GALLERYOPEN)
      }catch (e:Exception){
          e.printStackTrace()
      }
    }

    fun openCameraNow(){
        try {
            val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile:File=createImageFile()
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.simpleapp.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(intent, CAMERAOPEN)
            }
        }catch(e:Exception){
           e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    companion object{
        private const val CAMERAOPEN:Int=100
        private const val GALLERYOPEN:Int=200
    }


    fun setImagetoUI(myImage:String?){
        Glide.with(this@HomeActivity).load(myImage).into(imageView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK && requestCode== CAMERAOPEN){
           val myImage:String?= currentPhotoPath
           setImagetoUI(myImage)
        }else if(resultCode== RESULT_OK && requestCode== GALLERYOPEN){
            data?.let {
            val myImage:String= it.data.toString()
                currentPhotoPath=myImage
                setImagetoUI(myImage)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==Constants.CAMERA_CODE ){
            if(grantResults.size>0 && grantResults.get(0)==PackageManager.PERMISSION_GRANTED){
                openCameraNow()
            }else{
                Toast.makeText(this@HomeActivity,"Camera permission denied!",Toast.LENGTH_LONG).show()
            }
        }else if(requestCode==Constants.GALLERY_CODE){
            if(grantResults.size>0 && grantResults.get(0)==PackageManager.PERMISSION_GRANTED){
                openGalleryNow()
            }else{
                Toast.makeText(this@HomeActivity,"Gallery permission denied!",Toast.LENGTH_LONG).show()
            }
        }
    }

}