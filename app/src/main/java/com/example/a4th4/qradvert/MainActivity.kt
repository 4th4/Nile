package com.example.a4th4.qradvert

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import java.lang.Exception
import android.support.v7.app.AlertDialog
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Window
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import eliopi.nile.Poster
import java.io.*
import java.util.*

// Project synchronized with GitHub!!!
class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private var listAdvert: ArrayList<Advert> = ArrayList()
    private lateinit var fab: com.melnykov.fab.FloatingActionButton
    private lateinit var add: Button
    private lateinit var complete: Button
    private lateinit var edtTitle: EditText
    private lateinit var edtDescription: EditText
    private lateinit var frontDrop: RelativeLayout
    private lateinit var txtTitle: TextView
    private var flag: Boolean = false
    private lateinit var qrScan: IntentIntegrator
    private lateinit var queue : RequestQueue
    private val timeoutDuration = 10000
    private val gson: Gson = GsonBuilder().create()
    private lateinit var adapter:AdvertAdapter

    private val symbArr: Array<Array<String>> = Array(13) { Array(2) {""} }

    private val typeAdd = "add"
    private val typeView = "view"

    private val REPLACE_SPACE ="c3BhY2U"
    private val REPLACE_PERCENT ="cGVyY2VudA"
    private val REPLACE_SLASH_LEFT ="c2xhc2hsZWZ0"
    private val REPLACE_SLASH_RIGHT ="c2xhc2hyaWdodA"
    private val REPLACE_HASHTAG ="aGFzaHRhZw"
    private val REPLACE_DEGREE ="ZGVncmVl"
    private val REPLACE_KOVICHKA = "a292aWNoa2E"
    private val REPLACE_AND = "c3ltYm9sYW5k"
    private val REPLACE_QUESTION = "cXVlc3Rpb24"
    private val REPLACE_PALOCHKA =  "cGFsb2Noa2E"
    private val REPLACE_SQUARE_LEFT =  "c3F1YXJlbGVmdA"
    private val REPLACE_SQUARE_RIGHT =  "c3F1YXJlcmlnaHQ"
    private val REPLACE_FIGURE_LEFT =  "ZmlndXJlbGVmdA"
    private val REPLACE_FIGURE_RIGHT =  "ZmlndXJlcmlnaHQ"


    private val TAG_ENCODE="encode"
    private val TAG_DECODE="decode"

    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1

    private val fileName = "adverts.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        listView = findViewById(R.id.list_adverts)
        frontDrop = findViewById(R.id.frontdrop)
        txtTitle = findViewById(R.id.txt_title)
        complete = findViewById(R.id.complete)
        edtTitle = findViewById(R.id.edt_title)
        edtDescription = findViewById(R.id.edt_description)
        listView.divider = null
        checkPermission()
        readFile()
        adapter = AdvertAdapter(this, listAdvert)
        listView.adapter = adapter
        fab = findViewById(R.id.fab)
        fab.attachToListView(listView)
        add = findViewById(R.id.add)
        queue = Volley.newRequestQueue(this)
        qrScan = IntentIntegrator(this)
        addClick(add)
        startScan(fab)
        addAdvert(complete)
        listSelected(listView)
        removeAdvert()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
        if (result != null){
            if(result.contents != null){
               try {
                   viewAdvert(result.contents)
                   Toast.makeText(applicationContext,"Result is: " + result.contents,Toast.LENGTH_SHORT).show()
               }catch (e:Exception){
                   e.printStackTrace()
               }
            }else Toast.makeText(applicationContext,"Result not found!",Toast.LENGTH_LONG).show()
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS){
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                Toast.makeText(applicationContext,"Please accept this permission!" ,Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(null)
    }

    private fun formAdvert(title: String, description: String, id:Long): Advert {
        return Advert(title, description,id)
    }

    private fun addClick(view: View) {
        view.setOnClickListener {
            if (!flag) {
                openLayer(view)
            } else {
                closeLayer(view)
            }
        }
    }

    private fun closeLayer(view: View){
        txtTitle.text = "Adverts"
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f)
        rotation.duration = 350
        rotation.start()
        val translation = ObjectAnimator.ofFloat(frontDrop, "translationY", 0f)
        translation.duration = 350
        translation.start()
        listView.isEnabled = true
        listView.alpha = 1f
        flag = false
        edtTitle.text = null
        edtDescription.text = null
        edtTitle.requestFocus()
    }

    private fun openLayer(view: View){
        txtTitle.text = "Add advert"
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 225f)
        rotation.duration = 350
        rotation.start()
        val translation = ObjectAnimator.ofFloat(frontDrop, "translationY",
                (findViewById<LinearLayout>(R.id.backdrop)).height.toFloat())
        translation.duration = 350
        translation.start()
        listView.isEnabled = false
        listView.alpha = 0.5f
        flag = true
    }

    private fun startScan(view: View){
        view.setOnClickListener {
            qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            qrScan.setPrompt(" Scan a QR Code")
            qrScan.initiateScan()
        }
    }

    private fun generateQr(id: Long):Bitmap{
        val qrWriter = QRCodeWriter()
        return try {
            val bix = qrWriter.encode("$id", BarcodeFormat.QR_CODE,512,512)
            val width = bix.width
            val height = bix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            //return bmp
            bmp
        }catch (e: Exception) {
            e.printStackTrace()
            //return null(non-null)
            null!!
        }
    }

    private fun showQr(id: Long){
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val inflateView = inflater.inflate(R.layout.qr_dialog,null)
            val qrCode = generateQr(id)
            (inflateView.findViewById<ImageView>(R.id.imageView)).setImageBitmap(qrCode)
            shareQrCode((inflateView.findViewById<Button>(R.id.btn_save)),qrCode,id)
            builder.setView(inflateView)
            builder.show()
    }

    private fun shareQrCode(view: View, bitmap: Bitmap,id: Long){
        view.setOnClickListener{
            val file = File(externalCacheDir,"qrCode$id.png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.setReadable(true, false)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file))
            startActivity(Intent.createChooser(intent , "Share"))
        }
    }

    private fun generateUrl(type: String, s: String, description: String): String {
        val sr = deSymbol(s,TAG_ENCODE)
        val dr = deSymbol(description,TAG_ENCODE)
        return if (type == "add") {
            "https://nile-poster.herokuapp.com/add?name=$sr&about=$dr"
        }else{
            "https://nile-poster.herokuapp.com/view?id=$sr"
        }
    }

    private fun addAdvert(view: View){
        view.setOnClickListener { _ ->
            val dialog = dialogProgress()
            dialog.show()
            Log.d("SERVER", "AddAdvertMethod")
            val url = generateUrl(typeAdd,edtTitle.text.toString(),edtDescription.text.toString())
            Log.d("SERVER", "url $url")
            val stringRequest = StringRequest(Request.Method.POST, url,
                    Response.Listener<String> { response ->
                        dialog.cancel()
                        closeLayer(add)
                        // Display the first 500 characters of the response string.
                        Log.d("SERVER", "Result$response")
                        if (response != null) {
                            val resp = gson.fromJson(response, Poster::class.java)
                            showQr(resp.id)
                                listAdvert.add(0,formAdvert(deSymbol(resp.name,TAG_DECODE),
                                        deSymbol(resp.about,TAG_DECODE),resp.id))
                                listView.adapter = adapter
                                writeFile()
                        }
                    },
                    Response.ErrorListener {
                        dialog.cancel()
                        closeLayer(add)
                        Log.d("SERVER", "Result$it")
                    })
            stringRequest.retryPolicy = DefaultRetryPolicy(timeoutDuration,2,2.0f)
            queue.add(stringRequest)
        }
    }

    private fun viewAdvert(id:String){
        val dialog = dialogProgress()
        dialog.show()
        val stringRequest = StringRequest(Request.Method.POST, generateUrl(typeView,id,
                ""),
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    Log.d("SERVER", "Result$response")
                    dialog.cancel()
                    if (response != null && !response.isEmpty()){
                        val resp = gson.fromJson(response, Poster::class.java)
                        val fAdv = listAdvert.find {it.id == resp.id}
                        if(fAdv != null) {
                            Toast.makeText(applicationContext,"This advert is already exist!",Toast.LENGTH_SHORT).show()
                        }else{
                            listAdvert.add(0,formAdvert(deSymbol(resp.name,TAG_DECODE),
                                    deSymbol(resp.about,TAG_DECODE),resp.id))
                            listView.adapter = adapter
                            writeFile()
                        }
                    }else{Toast.makeText(applicationContext,"This advert doesn't exist!",Toast.LENGTH_SHORT).show()}
                },
                Response.ErrorListener {
                    Log.d("SERVER", "Result$it")
                    dialog.cancel()
                })
        stringRequest.retryPolicy = DefaultRetryPolicy(timeoutDuration,2,2.0f)
        queue.add(stringRequest)
    }

    private fun listSelected(view: ListView){
        view.onItemClickListener = AdapterView.OnItemClickListener{parent, view, position, id ->
            showQr(listAdvert[position].id)
        }
    }

    private fun deSymbol(s:String, tag:String): String{
        formSymbArr()
        var str = s
        var x = 0
        try {
            return if(tag == TAG_ENCODE){
                while (x < symbArr.size){
                    str = str.replace(symbArr[x][0],symbArr[x][1])
                    x++
                }
                str
            }else{
                while (x < symbArr.size){
                    str = str.replace(symbArr[x][1],symbArr[x][0])
                    x++
                }
                str
            }
        }catch (e:Exception){
            e.printStackTrace()
            return str
        }
    }

    private fun formSymbArr(){
        symbArr[0] = arrayOf("%",REPLACE_PERCENT)
        symbArr[1] = arrayOf(" ",REPLACE_SPACE)
        symbArr[2] = arrayOf("#",REPLACE_HASHTAG)
        symbArr[3] = arrayOf("&",REPLACE_AND)
        symbArr[4] = arrayOf("/",REPLACE_SLASH_RIGHT)
        symbArr[5] = arrayOf("^",REPLACE_DEGREE)
        symbArr[6] = arrayOf("`",REPLACE_KOVICHKA)
        symbArr[7] = arrayOf("?",REPLACE_QUESTION)
        symbArr[8] = arrayOf("|",REPLACE_PALOCHKA)
        symbArr[9] = arrayOf("[",REPLACE_SQUARE_LEFT)
        symbArr[10] = arrayOf("]",REPLACE_SQUARE_RIGHT)
        symbArr[11] = arrayOf("{",REPLACE_FIGURE_LEFT)
        symbArr[12] = arrayOf("}",REPLACE_FIGURE_RIGHT)
        symbArr[4] = arrayOf("\\",REPLACE_SLASH_LEFT)
    }

    private fun readFile(){
        Log.d("SERVER","read file")
        try {
            val reader = FileReader(File(Environment.getExternalStorageDirectory(),fileName))
            listAdvert = gson.fromJson(reader.readText(),
                    object : TypeToken<ArrayList<Advert>>(){}.type) as ArrayList<Advert>
            reader.close()
        }catch (e:Exception){e.printStackTrace()
            Log.d("SERVER","Exception = $e")
        }
    }

    private fun writeFile(){
        val file = File(Environment.getExternalStorageDirectory(),fileName)
        Log.d("SERVER","onDestroy FILE PATH " + file.absolutePath)
        val writer = FileWriter(file)
        Log.d("SERVER","List is " + gson.toJson(listAdvert))
        writer.write(gson.toJson(listAdvert))
        writer.flush()
        writer.close()
    }

    private fun dialogProgress() : Dialog {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_progress,null)
        view.findViewById<ProgressBar>(R.id.progressBar)
                .indeterminateDrawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(applicationContext, R.color.colorPrimary),
                PorterDuff.Mode.MULTIPLY)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        dialog.window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(applicationContext, android.R.color.transparent)))
        dialog.setContentView(view)
        dialog.setCancelable(false)
        return dialog
    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS)
                //запрашиваю разрешение на использование внутрениго хранилища
            }
        } else {
            // Permission has already been granted
        }
    }

    private fun removeAdvert(){
          listView.onItemLongClickListener = AdapterView.OnItemLongClickListener{
              parent, view, position, id ->
              dialogRemove(position).show()
              true
          }
    }

    private fun dialogRemove(position: Int) : Dialog {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_remove,null)
        view.findViewById<Button>(R.id.remove).setOnClickListener {
            listAdvert.removeAt(position)
            listView.adapter = adapter
            writeFile()
            dialog.cancel()
        }
        view.findViewById<Button>(R.id.cancel).setOnClickListener {
            dialog.cancel()
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        dialog.window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(applicationContext, android.R.color.transparent)))
        dialog.setContentView(view)
        dialog.setCancelable(false)
        return dialog
    }
}

