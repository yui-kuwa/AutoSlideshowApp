package jp.techacademy.yui.kuwahara.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.icu.text.AlphabeticIndex
import android.net.Uri
import android.transition.Slide
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.os.Handler

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100

    var backButtonFlag: Boolean = false
    var nextButtonFlag: Boolean = false
    var startStopButtonFlag: Boolean = true//trueがstart falseがstop
    var ContentsInfoFlag: Boolean = false

    var uriList = mutableListOf<Uri>()//Uriを格納する配列を作成
    var list_Size: Int = 0//配列の大きさ
    var num: Int = 0//現在の配列の要素

    private var mTimer: Timer? = null
    private var mTimerSec = 0.0// タイマー用の時間のための変数
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
                ContentsInfoFlag = true
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
            ContentsInfoFlag = true
        }

        //backボタン
        back_button.setOnClickListener(this)

        //nextボタン
        next_button.setOnClickListener(this)

        //start,stopボタン
        start_stop_button.setOnClickListener(this)

    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }*/

    private fun getContentsInfo() {
        // 画像の情報を取得する
        var cnt: Int = 0
        var resolver = contentResolver
        var cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {//検索結果の最初のデータを指す
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                var fieldIndex =
                    cursor.getColumnIndex(MediaStore.Images.Media._ID)//現在cursorが指しているデータの中から画像のIDがセットされている位置を取得
                var id = cursor.getLong(fieldIndex)//画像のIDを取得
                var imageUri =
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )//実際の画像のURIを取得

                uriList.add(cnt, imageUri)//Uriを順に格納
                //uriList[cnt] = imageUri
                //Log.d("ANDROID", "URI : " + imageUri.toString())
                cnt++
                list_Size = cnt

            } while (cursor.moveToNext())
        }
        image.setImageURI(uriList[0])//初めの画像だけ表示
        cursor!!.close()
    }

    private fun imageSlide() {//ボタンで画像遷移
        if(backButtonFlag == true){//backボタンが押されたら
            if(num != 0) {//現在表示している画像が1番目（uriList[0]）でない時
                num--//1つ前の画像を表示
                image.setImageURI(uriList[num])
            }else{//現在表示している画像が1番目の時
                num = list_Size - 1
                image.setImageURI(uriList[num])//最後の画像を表示
            }
                backButtonFlag = false
        }else if(nextButtonFlag == true){//nextボタンが押されたら
            if(num == list_Size - 1){//現在表示している画像が最後の画像の時
                num = 0
                image.setImageURI((uriList[num]))
            }else {
                num++ //次の画像を表示
                image.setImageURI(uriList[num])
            }
            nextButtonFlag = false
        }
    }

    private fun slideTimer(){
        // タイマーの始動
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mTimerSec += 1
            }
        }, 0, 1000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
    }

    private fun autoImageSlide(){//自動で画像遷移
        if(!startStopButtonFlag) {//startボタンを押した時
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mTimerSec += 2
                    mHandler.post {
                        if (num == list_Size - 1) {//現在表示している画像が最後の画像の時
                            num = 0
                            image.setImageURI((uriList[num]))
                        } else {
                            num++ //次の画像を表示
                            image.setImageURI(uriList[num])
                        }
                    }
                }
            }, 2000, 2000) // 最初に始動させるまで2000ミリ秒、ループの間隔を2000ミリ秒 に設定

            nextButtonFlag = false
        }
    }

    override fun onClick(v: View) {
        // パーミッションが許可されている
        if(ContentsInfoFlag) {

            //backボタンが押されたら
            if (v == back_button && startStopButtonFlag == true) {
                backButtonFlag = true
                imageSlide()
            } else if (v == next_button && startStopButtonFlag == true) {//nextボタンが押されたら
                nextButtonFlag = true
                imageSlide()
            } else if (v == start_stop_button) {//startボタン、stopボタンが押されたら
                if (startStopButtonFlag) {//スライドショーがstopしてる状態
                    startStopButtonFlag = false
                    start_stop_button.text = "Stop"
                    mTimer = Timer()//タイマーの作成
                    autoImageSlide()
                } else {//スライドショーがstartしている状態
                    startStopButtonFlag = true
                    start_stop_button.text = "Start"
                    mTimer!!.cancel()
                    mTimerSec = 0.0
                }
            }
        }
    }
}

