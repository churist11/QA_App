package jp.techacademy.shoutarou.sakurai.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavListActivity : AppCompatActivity() {

    // プロパティ
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter
    private var mFavKeyArrayList = ArrayList<String>()

    // リスナー
    private val mFavDataListener = object : ValueEventListener {

        override fun onDataChange(p0: DataSnapshot) {

            // リセット
            mFavKeyArrayList.clear()

            for (uidSnapshot in p0.children) {

                for (qidSnapshot in uidSnapshot.children) {

                    // お気に入りリストのプロパティに取得したキーを追加、保持
                    mFavKeyArrayList.add(qidSnapshot.key!!)

                }
            }

            Log.d("DEDE","${mFavKeyArrayList}")

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    private val mContentsListener = object : ValueEventListener {
        override fun onDataChange(p0: DataSnapshot) {

            for (genreSnapshot in p0.children) {

                for (quidSnapshot in genreSnapshot.children) {
//                    Log.d("DEDE","mContentsListener:${quidSnapshot.key}")

                    // データのキーとmFavKeyArrayListを比較し、合致するデータを抽出
                    for (favKey in mFavKeyArrayList) {

                        if (quidSnapshot.key == favKey) {
//                            Log.d("DEDE", "MATCHED")

                            // 合致したデータのvalueをmapに変換
                            val map = quidSnapshot.value as Map<String, String>

                            // Questionオブジェクトに変換,mQuestionArrayListにオブジェクトを追加
                            convertToQuestionArray(map, quidSnapshot)

                        } else {
//                            Log.d("DEDE", "NOT MATCHED")
                        }

                    }
                }
            }

            Log.d("DEDE", "${mQuestionArrayList}")
        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_list)

        // ログイン中のユーザーを取得
        val user = FirebaseAuth.getInstance().currentUser

        // 画面のタイトルを設定
        if (user != null) {

            // Preferenceから表示名を取得してActivityのtitleに反映させる
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val name = sp.getString(NameKEY, "")
            title = "${name!!}さんのお気に入りリスト"

        } else {

            // デフォルトのタイトル
            title = "お気に入りリスト"

        }

        // <Listの初期化>
        mQuestionArrayList = ArrayList<Question>()


        // 読み込み中、インジケータを表示して、取得成功後に、以後の処理を行う
        // 取得に失敗の場合は、// プログレスバーを非表示にする
        //                progressBar.visibility = View.GONE・VISIBLE
        //
        //                // Activityを閉じる
        //                finish()

        // 参照
        mDatabaseReference = FirebaseDatabase.getInstance(URL).reference
        val favRef = mDatabaseReference.child(UsersPATH).child(user!!.uid).child("favorites")
        val contentsRef = mDatabaseReference.child(ContentsPATH)

        // ログイン中の"favorite"にリスナーを設定する
        favRef.addListenerForSingleValueEvent(mFavDataListener)

        // 質問コンテンツ全てにリスナーを設定する
        contentsRef.addListenerForSingleValueEvent(mContentsListener)

        // Adapterの準備
        mAdapter = QuestionsListAdapter(this)
        mAdapter.setQuestionArrayList(mQuestionArrayList)

        // ListViewの準備
        mListView = findViewById<ListView>(R.id.listView)
        mListView.adapter = mAdapter

        // データの更新
        mAdapter.notifyDataSetChanged()
    }

    private fun convertToQuestionArray(map : Map<String, String>, dataSnapshot : DataSnapshot) {
        val title = map["title"] ?: ""
        val body = map["body"] ?: ""
        val name = map["name"] ?: ""
        val uid = map["uid"] ?: ""
        val imageString = map["image"] ?: ""
        val bytes =
            if (imageString.isNotEmpty()) {
                Base64.decode(imageString, Base64.DEFAULT)
            } else {
                byteArrayOf()
            }

        val answerArrayList = ArrayList<Answer>()
        val answerMap = map["answers"] as Map<String, String>?
        if (answerMap != null) {
            for (key in answerMap.keys) {
                val temp = answerMap[key] as Map<String, String>
                val answerBody = temp["body"] ?: ""
                val answerName = temp["name"] ?: ""
                val answerUid = temp["uid"] ?: ""
                val answer = Answer(answerBody, answerName, answerUid, key)
                answerArrayList.add(answer)
            }
        }

        val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
            0, bytes, answerArrayList)
        mQuestionArrayList.add(question)
        mAdapter.notifyDataSetChanged()
    }
}
