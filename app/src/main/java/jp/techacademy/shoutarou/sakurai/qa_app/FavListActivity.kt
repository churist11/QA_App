package jp.techacademy.shoutarou.sakurai.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_setting.*

class FavListActivity : AppCompatActivity() {

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

    }
}
