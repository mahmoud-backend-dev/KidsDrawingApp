package com.example.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var mImageButtonCurrentPaint:ImageButton?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawing_view.setSizeForBrush(20.toFloat())
        mImageButtonCurrentPaint=ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
        ib_gallery.setOnClickListener {
            if(isReadPermissionAllowed()){
                // run our code to get the image from the gallery
                val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickIntent, GALLERY)
            }else{
            requestStoragePermission()
            }
        }
        ib_undo.setOnClickListener {
            drawing_view.undoClick()
        }
        ib_sava.setOnClickListener {
            if(isReadPermissionAllowed()){
                BitmapAsyncTask(getBitmapFromView(fl_drawing_view_container)).execute()
            }else{
                requestStoragePermission()
            }
        }
    }
    private inner class BitmapAsyncTask(val mBitmap:Bitmap):AsyncTask<Any,Void,String>(){
        private lateinit var mProgressDialog: Dialog
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }
        override fun doInBackground(vararg params: Any?): String {
            var result=""
            if(mBitmap!=null){
                try {
                    val bytes=ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f=File(externalCacheDir!!.absoluteFile.toString()+File.separator
                    +"KidDrawingApp_"+System.currentTimeMillis()/1000+".png")
                    val fos=FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    result=f.absolutePath
                }catch (e:Exception){}
            }
            return result
        }
        private fun getImageUri(inContext:Context,inImage:Bitmap):Uri?{
            val byte=ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG,100,byte)
            val path= MediaStore.Images.Media.insertImage(inContext.contentResolver,inImage,"Title",null)
            return Uri.parse(path)
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (!result!!.isEmpty()){
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_LONG
                ).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path,uri-> val shereIntent=Intent()
                val image :Bitmap?=getBitmapFromView(fl_drawing_view_container)
                shereIntent.action=Intent.ACTION_SEND
                shereIntent.type="image/png"
                shereIntent.putExtra(Intent.EXTRA_STREAM,getImageUri(this@MainActivity,image!!))
                startActivity(Intent.createChooser(
                    shereIntent,"Share"
                ))
            }

        }
        private fun showProgressDialog() {
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)
            mProgressDialog.show()
        }
        private fun cancelProgressDialog() {
            mProgressDialog.dismiss()
        }
    }
    private fun showBrushSizeChooserDialog()
    {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        val small=brushDialog.ib_small_brush
        small.setOnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val meduim =brushDialog.ib_medium_brush
        meduim.setOnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val large=brushDialog.ib_large_brush
        large.setOnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }
   fun paintClicked(view: View){
       if(view != mImageButtonCurrentPaint){
           val imageButton=view as ImageButton
           val colorTag=imageButton.tag.toString()
           drawing_view.setColor(colorTag)
           imageButton.setImageDrawable(
               ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
           )
           mImageButtonCurrentPaint!!.setImageDrawable(
               ContextCompat.getDrawable(this,R.drawable.pallet_normal)
           )
           mImageButtonCurrentPaint=view
       }
   }
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).toString()
            )){
            Toast.makeText(this,"Need Permission to add a background",Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERNISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==Activity.RESULT_OK){
            if (requestCode== GALLERY){
                try {
                    if(data!!.data!=null){
                        iv_background.visibility=View.VISIBLE
                        iv_background.setImageURI(data.data)
                    }else{
                        Toast.makeText(this,
                            "Error in parsing the image or its corrupted",
                            Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception){}
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== STORAGE_PERNISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                    "Permission granted now you can read the storage files.",
                    Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this,
                    "Oops you just denied the permission.",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun isReadPermissionAllowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        return result==PackageManager.PERMISSION_GRANTED
    }
    private fun getBitmapFromView(view:View):Bitmap{
        val returnBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(returnBitmap)
        val bgDrawable=view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmap
    }


    companion object{
        private const val STORAGE_PERNISSION_CODE=1
        private const val GALLERY=2
    }
}