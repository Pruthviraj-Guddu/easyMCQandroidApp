@file:Suppress("DEPRECATION")

package com.guddu.demoasynctask

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Build.*
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var context: Context
    var questionList : MutableList<Question> = ArrayList()
    var index= -1
    var score = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        btn_next.isEnabled=false
        btn_next.alpha - 0.5.toFloat()
        GetQuestion().execute()
    }
    @SuppressLint("SetTextI18n")

    fun updateQuestion(){
        val selected = rg_choice.checkedRadioButtonId
        if(selected==-1){
            Toast.makeText(this,"Please select an answer.",Toast.LENGTH_SHORT).show()
            return
        }
        if(index<questionList.size){
            when(selected){
                rb_choice1.id ->{
                    if(questionList[index].Answer ==1)
                        score++
                }
                rb_choice2.id ->{
                    if(questionList[index].Answer ==2)
                        score++
                }
                rb_choice3.id ->{
                    if(questionList[index].Answer ==3)
                        score++
                }
                rb_choice4.id ->{
                    if(questionList[index].Answer ==4)
                        score++
                }

            }
            index++
            if (index<questionList.size){
                tv_question.text = questionList[index].Question
                rb_choice1.text= questionList[index].Option1
                rb_choice2.text= questionList[index].Option2
                rb_choice3.text= questionList[index].Option3
                rb_choice4.text= questionList[index].Option4
                rg_choice.clearCheck()
                if ((index+1)==questionList.size)
                    btn_next.text="Finish"
            }else{
                val dialog = AlertDialog.Builder(context)
                dialog.setTitle("Result..")
                dialog.setMessage(" you have correctly answered " + score + " out of " +questionList.size + " Questions " )
                dialog.setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss()
                    finish()
                }
                dialog.show()
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class GetQuestion : AsyncTask<Void,Void,String>(){

        lateinit var progressDialog : ProgressDialog
        var hasInternet = false

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Downloading Question")
            progressDialog.setCancelable(false)
            progressDialog.show()
            //println("Hello till downloading question")

        }
        @RequiresApi(VERSION_CODES.CUPCAKE)
        public override fun doInBackground(vararg params: Void?): String {
            if (isNetworkAvailable()){
                hasInternet = true
                val client = OkHttpClient()
                var url : String = "https://script.googleusercontent.com/macros/echo?user_content_key=1tgBN-ES-vsiLin8Lggs7R094sUSEWlBY3Lv7yLt0KnrexUuaTvreORsTenxGH0HaPDQ0rUkXVqmkc903P_gQrpXCbi98gcsm5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnBg4Wj9So2Q_mI0_S0Bm21-AGmcRnplmVaRcxvVzvCi9cnQQJegsnVb9TgJzPufw35cdv3aNHr6K&lib=MKMzvVvSFmMa3ZLOyg67WCThf1WVRYg6Z"
                var request = Request.Builder().url(url).build()
                var response = client.newCall(request).execute()
                println("quest down \n\n\n")
                println(response)
                return response.body?.string().toString()
            }
            else
            {
                //return ""
//                val duration = Toast.LENGTH_SHORT
//
//                val toast = Toast.makeText(applicationContext, "Cant connect Internet", duration)
//                toast.show()

//                this.progressDialog = ProgressDialog(context)
//                this.progressDialog.setMessage("No internet")
//                this.progressDialog.setCancelable(false)
//                this.progressDialog.show()
                return ""

            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            if(hasInternet){
                try{
                    var resultArray = JSONArray(result)
                    println("***********resultArray*************")
                    println(resultArray)
                    for (i in 0 until resultArray.length()){
                        var currentObject = resultArray.getJSONObject(i)
                        var obj = Question()
                        //println("hello Worls")
                        obj.Question=currentObject.getString("Question")
                        obj.Option1=currentObject.getString("Option1")
                        obj.Option2=currentObject.getString("Option2")
                        obj.Option3=currentObject.getString("Option3")
                        obj.Option4=currentObject.getString("Option4")
                        obj.Answer=currentObject.getInt("Answer")
                        questionList.add(obj)



                    }
                    if(index==-1){
                        index++
                        tv_question.text = questionList[index].Question
                        rb_choice1.text= questionList[index].Option1
                        rb_choice2.text= questionList[index].Option2
                        rb_choice3.text= questionList[index].Option3
                        rb_choice4.text= questionList[index].Option4
                    }
                    btn_next.isEnabled=true
                    btn_next.alpha=1.toFloat()
                    btn_next.setOnClickListener {
                        updateQuestion()
                    }

                }catch (e: JSONException){

                }
            }

        }

    }



    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }



}



