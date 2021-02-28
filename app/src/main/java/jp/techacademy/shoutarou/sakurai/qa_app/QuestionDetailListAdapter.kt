package jp.techacademy.shoutarou.sakurai.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.HashMap
import com.google.firebase.database.ValueEventListener



class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null

//    private var mFavList = mutableListOf<String>()
    private var mFavMap = mutableMapOf<String, String?>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQuestion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            // お気に入りボタンの設定
            val favButton = convertView.findViewById<View>(R.id.favButton) as Button

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログイン中のみボタンを表示
            if (user == null) {
                // 未ログイン状態
                favButton.isEnabled = false
                favButton.alpha = 0f
            } else {
                // ログイン状態

                // ボタンを有効化
                favButton.isEnabled = true
                favButton.alpha = 1f

                // 参照
                val dataBaseReference = FirebaseDatabase.getInstance(URL).reference
                val userRef = dataBaseReference.child(UsersPATH).child(user!!.uid)
                val favRef = userRef.child("favorites")
                val uidRef = favRef.child(mQuestion.uid)
                val qidRef = uidRef.child(mQuestion.questionUid)

                // Value リスナーの定義
                val listener = object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        Log.d("DEBUG", "onDataChange: ${p0}")

                        // スナップショットからこの参照のデータにvalueが存在するかを確認
                            if (p0.value == null) {
                                // <未登録の場合>

                                // この場合のボタン表示
                                favButton.text = "お気に入りに追加"

                                // ボタンにお気に入りに追加する処理を設定
                                favButton.setOnClickListener {

                                    // valueをFirebaseに保存
                                    qidRef.push().setValue(mQuestion.questionUid).addOnSuccessListener {
                                        // ボタンの表示を切り替える
                                        favButton.text = "お気に入りを解除"
                                    }.addOnFailureListener {
                                        // エラーを表示する
                                        Snackbar.make(parent, "お気に入りの追加に失敗しました", Snackbar.LENGTH_LONG).show()
                                    }

                                }

                            } else {
                                // <登録済の場合>

                                // この場合のボタン表示
                                favButton.text = "お気に入りを解除"

                                // ボタンにお気に入りから削除する処理を設定
                                favButton.setOnClickListener {

                                    // valueをFirebaseから削除
                                    qidRef.removeValue().addOnSuccessListener {
                                        // ボタンの表示を切り替える
                                        favButton.text = "お気に入りに追加"
                                    }.addOnFailureListener {
                                        // エラーを表示する
                                        Snackbar.make(parent, "お気に入りの解除に失敗しました", Snackbar.LENGTH_LONG).show()
                                    }

                                }

                            }

                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.d("DEBUG", "onCancelled: ${p0}")
                    }
                }

                // Value リスナーの設定
                qidRef.addValueEventListener(listener)
            }

        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }

        return convertView
    }
}