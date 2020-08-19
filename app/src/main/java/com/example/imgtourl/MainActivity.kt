package com.example.imgtourl

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() , AdapterView.OnItemSelectedListener{
    private var imagePreview: ImageView? = null
    private val PICK_IMAGE_REQUEST = 1
    private var filePath: Uri? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    var list = ArrayList<String>()
    private var selectedFolder : String? = null
    var url:StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Convert image to URL"
        setContentView(R.layout.activity_main)

        imagePreview = findViewById<ImageView>(R.id.imagePreview) as ImageView
        val chooseImageBtn = findViewById<Button>(R.id.choose_image) as Button
        val uploadImageBtn = findViewById<Button>(R.id.upload_image) as Button
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        chooseImageBtn.setOnClickListener { imagePicker() }
        uploadImageBtn.setOnClickListener { uploadImage() }


        val listRef = FirebaseStorage.getInstance().getReference("/")

        val spinnerFolder: Spinner? = findViewById(R.id.folder_spinner)


        spinnerFolder?.setSelection(1)
        spinnerFolder?.onItemSelectedListener = this@MainActivity

        var adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFolder?.adapter = adapter
        adapter.notifyDataSetChanged()


        listRef.listAll()
            .addOnSuccessListener { listResult ->
                list.add("None")
                Log.d("FragmentActivity listRef.name", listRef.name)
                for (prefix in listResult.prefixes) {

                    list.add(prefix.name)
                    Log.d("FragmentActivity prefix.name", prefix.name)

                }
                adapter = ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, list)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerFolder?.adapter = adapter
                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
            }


        url_text.setOnClickListener {

                val clipboard: ClipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label.toString(), url_text.text)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(
                    this, "URL copied to clipboard ",
                    Toast.LENGTH_SHORT
                ).show()
        }


    }



    private fun imagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture")
            , PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }
            filePath = data.data
            try {
                val bitmap = MediaStore.Images
                    .Media.getBitmap(contentResolver, filePath)
                imagePreview?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
//        val folderName = folder_name.text.toString()
        if(selectedFolder == "None")
            selectedFolder = folder_name.text.toString()
        if(filePath != null){
            val ref = storageReference
                                        ?.child(selectedFolder +"/"
                                                + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)
            uploadTask?.continueWith {
                if (!it.isSuccessful) {
//                    pd.dismiss()
                    it.exception?.let { t ->
                        throw t
                    }
                }
                ref.downloadUrl
            }?.addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result!!.addOnSuccessListener { task ->
                        val myUri = task.toString()
                        url_text.text = myUri
                        Log.i("urllllll","urllllll$myUri")
                        Toast.makeText(
                            this,
                            "Image Uploaded", Toast.LENGTH_SHORT
                        ).show()
                    }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this, "Image Uploading Failed " + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
        else{
            Toast.makeText(this, "Please Select an Image",
                Toast.LENGTH_SHORT).show()
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedFolder = folder_spinner.selectedItem.toString()
        Log.i("add", "--------spinnerFolder-------------- $selectedFolder")
/*        Toast.makeText(
            this,
            "Selected item$selectedFolder",
            Toast.LENGTH_SHORT).show()*/

        if(selectedFolder == "None"){
            Log.i("folder", "--------Folder none-------------- $selectedFolder")
            folder_name.inputType = InputType.TYPE_CLASS_TEXT
        }
        else{
            Log.i("folder", "--------Folder not none-------------- $selectedFolder")
            folder_name.inputType = InputType.TYPE_NULL
            folder_name.setText("")
        }


    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }


}