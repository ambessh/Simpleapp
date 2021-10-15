package com.example.simpleapp.view.activities

import android.R.attr
import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.simpleapp.R
import android.graphics.Bitmap

import android.os.Environment

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri

import android.os.Build
import android.util.Log

import androidx.annotation.NonNull
import androidx.core.graphics.drawable.toBitmap
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import android.R.attr.bitmap
import android.graphics.Matrix
import com.bumptech.glide.load.resource.bitmap.TransformationUtils

import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import android.R.attr.bitmap
import android.content.Intent
import androidx.core.net.toUri
import com.bumptech.glide.load.engine.DiskCacheStrategy

import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import com.theartofdev.edmodo.cropper.CropImageView
import java.security.AccessController.getContext


class EditActivity : AppCompatActivity(),View.OnClickListener {

    lateinit var imageViewEdit:ImageView
    lateinit var undo:Button
    lateinit var rotate:Button
    lateinit var crop:Button
    lateinit var save:Button
    lateinit var cropnow:Button
    var imagePath: String?=null
    var bimp:Bitmap?=null  // for rotated image
    var myBitmap:Bitmap?=null //for without rotated image
    var bitmapCopy:Bitmap?=null // copy of for without rotated image
    lateinit var cropImageView:CropImageView
    var mAngle=0
     var finalImage:Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.setTitle("Edit")

        imageViewEdit=findViewById(R.id.imageViewEdit)
        undo=findViewById(R.id.undo)
        rotate=findViewById(R.id.rotate)
        crop=findViewById(R.id.crop)
        save=findViewById(R.id.save)
        cropImageView=findViewById(R.id.cropImageView)
        cropnow=findViewById(R.id.cropnow)

        undo.setOnClickListener(this)
        rotate.setOnClickListener(this)
        crop.setOnClickListener(this)
        save.setOnClickListener(this)
        cropnow.setOnClickListener(this)
        showImagePreview()


    }


    fun showImagePreview(){
         imagePath = intent.getStringExtra("imagepath")  // from camera

         if(imagePath!!.startsWith("content:")) {      // from gallery image path
             Glide.with(this@EditActivity).load(imagePath).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                 .listener(object : RequestListener<Drawable>{
                     override fun onLoadFailed(
                         e: GlideException?,
                         model: Any?,
                         target: Target<Drawable>?,
                         isFirstResource: Boolean
                     ): Boolean {
                         return false
                     }

                     override fun onResourceReady(
                         resource: Drawable?,
                         model: Any?,
                         target: Target<Drawable>?,
                         dataSource: DataSource?,
                         isFirstResource: Boolean
                     ): Boolean {
                         if(resource!=null){
                             myBitmap=resource.toBitmap()
                             bitmapCopy = myBitmap!!.copy(myBitmap!!.getConfig(), true);
                       // from gallery bitmap
                         }
                         return false
                     }

                 })
                 .into(imageViewEdit)

         }else{
             Glide.with(this@EditActivity).load(imagePath)
                 .listener(object:RequestListener<Drawable>{
                     override fun onLoadFailed(
                         e: GlideException?,
                         model: Any?,
                         target: Target<Drawable>?,
                         isFirstResource: Boolean
                     ): Boolean {
                         return false
                     }

                     override fun onResourceReady(
                         resource: Drawable?,
                         model: Any?,
                         target: Target<Drawable>?,
                         dataSource: DataSource?,
                         isFirstResource: Boolean
                     ): Boolean {
                         if(resource!=null) {
                             myBitmap = resource.toBitmap()
                             bitmapCopy = myBitmap!!.copy(myBitmap!!.getConfig(), true);
                        // from camera bitmap
                         }
                         return false
                     }

                 })

                 .into(imageViewEdit)
         }


    }

    override fun onClick(v: View?) {
        if(v!=null){
            when(v.id){
                R.id.undo->{
                    undoFun()
                    return
                }
                R.id.rotate->{
                  rotateHandler()
                    return
                }
                R.id.crop->{
                  startCrop()
                    return
                }
                R.id.save->{
                    saveImageToGallery()
                    return
                }
                R.id.cropnow->{
                    if(bimp!=null){
                        bimp = cropImageView.croppedImage
                        bitmapCopy=bimp
                        Glide.with(this).load(bimp).into(imageViewEdit)



                    }else {
                            bitmapCopy = cropImageView.croppedImage
                            Glide.with(this).load(bitmapCopy).into(imageViewEdit)

                    }
                    cropImageView.visibility=View.GONE
                    cropnow.visibility=View.GONE
                    imageViewEdit.visibility=View.VISIBLE
                    crop.isEnabled=true
                    rotate.isEnabled=true
                    undo.isEnabled=true
                    save.isEnabled=true

                    return
                }
            }
        }
    }




    fun startCrop(){
        cropImageView.visibility=View.VISIBLE
        if(bimp!=null){ // image is rotated
            cropImageView.setImageBitmap(bimp)


        }else{
                cropImageView.setImageBitmap(bitmapCopy)

        }
        imageViewEdit.visibility=View.GONE
        cropnow.visibility=View.VISIBLE
        crop.isEnabled=false
        rotate.isEnabled=false
        undo.isEnabled=false
        save.isEnabled=false

    }

    fun undoFun(){
    mAngle=mAngle-90
        Log.i("a","a")
        bimp=  rotateImageNow(bitmapCopy!!,mAngle.toFloat())
        Glide.with(this@EditActivity).load(bimp).into(imageViewEdit)

        if(mAngle==-360){
            mAngle=0
        }

    }

    fun rotateHandler(){
        mAngle=mAngle+90


            Log.i("a","a")
            bimp=  rotateImageNow(bitmapCopy!!,mAngle.toFloat())
            Glide.with(this@EditActivity).load(bimp).into(imageViewEdit)


        if(mAngle==360){
            mAngle=0
        }
    }



    fun saveImageToGallery(){

        if(bimp!=null){  //saving file after rotation
            saveImage(bimp!!, "fileNew")
            Toast.makeText(this@EditActivity, "Saved to gallery!", Toast.LENGTH_SHORT).show()
          moveBack()
        }else {
            saveImage(bitmapCopy!!, "fileNew")
                Toast.makeText(this@EditActivity, "Saved to gallery!", Toast.LENGTH_SHORT).show()
             moveBack()
        }

    }

    fun moveBack(){
        if(finalImage!=null) {
            val intent = Intent(
                this@EditActivity,
                HomeActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("returnedImage", finalImage)
            startActivity(intent)
            finish()
        }
    }


    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap, name: String) {
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri: Uri? =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = Objects.requireNonNull(imageUri)?.let { resolver.openOutputStream(it) }
             finalImage=imageUri
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val image = File(imagesDir, "$name.jpg")

            fos = FileOutputStream(image)
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        Objects.requireNonNull(fos)?.close()
    }


    fun rotateImageNow(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }



}