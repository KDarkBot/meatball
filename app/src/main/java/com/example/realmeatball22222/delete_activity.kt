package com.example.realmeatball22222

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.realmeatball22222.databinding.ActivityDeleteBinding
import com.example.realmeatball22222.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class delete_activity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var editCategory: EditText
    private lateinit var editPercent: EditText
    private lateinit var editCategoryToDelete: EditText // 삭제할 카테고리 이름 입력받는 EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var deleteAllButton: Button // 전체 데이터 삭제 버튼
    private lateinit var radioGroup: RadioGroup
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityDeleteBinding
    private var selectedGraph = "graph1" // 기본 선택된 그래프
    private var userId: String? = null // 사용자 ID (익명 사용자 고유 ID)
    override fun onResume() {
        super.onResume()

        // 화면이 다시 활성화될 때마다 데이터를 불러옴
        userId?.let { loadDataFromFirebase(it, selectedGraph) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDeleteBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.savegoto.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
        }
        // Firebase Authentication 초기화
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 익명 로그인 성공
                    val user = FirebaseAuth.getInstance().currentUser
                    userId = user?.uid
                    userId?.let { loadDataFromFirebase(it, selectedGraph) }

                } else {
                    // 로그인 실패 시 처리
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

        // XML 요소 연결
        pieChart = findViewById(R.id.pieChart)

        editCategoryToDelete = findViewById(R.id.editCategoryToDelete) // 삭제할 카테고리 입력

        deleteButton = findViewById(R.id.deleteButton)
        deleteAllButton = findViewById(R.id.deleteAllButton) // 전체 데이터 삭제 버튼
        radioGroup = findViewById(R.id.radioGroup)

        // Firebase Database 초기화
        database = FirebaseDatabase.getInstance().reference

        // 기본 데이터 로드
        userId?.let { loadDataFromFirebase(it, selectedGraph) }

        // 라디오 그룹 선택 이벤트
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedGraph = when (checkedId) {
                R.id.radio1 -> "graph1"
                R.id.radio2 -> "graph2"
                R.id.radio3 -> "graph3"
                R.id.radio4 -> "graph4"
                R.id.radio5 -> "graph5"
                R.id.radio6 -> "graph6"
                R.id.radio7 -> "graph7"
                else -> "graph1"
            }
            userId?.let { loadDataFromFirebase(it, selectedGraph) }
        }



        // 삭제 버튼 클릭 이벤트
        deleteButton.setOnClickListener {
            val categoryNameToDelete = editCategoryToDelete.text.toString().trim()

            if (categoryNameToDelete.isEmpty()) {
                Toast.makeText(this, "삭제할 카테고리 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userId?.let { deleteCategoryFromFirebase(it, selectedGraph, categoryNameToDelete) }
        }

        // 전체 데이터 삭제 버튼 클릭 이벤트
        deleteAllButton.setOnClickListener {
            userId?.let { deleteAllDataFromFirebase(it, selectedGraph) }
        }
    }



    // Firebase에서 데이터를 불러오는 메소드
    private fun loadDataFromFirebase(userId: String, graph: String) {
        database.child("users").child(userId).child(graph).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pieEntries = ArrayList<PieEntry>()

                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key ?: ""
                    val value = categorySnapshot.getValue(Float::class.java) ?: 0f
                    pieEntries.add(PieEntry(value, categoryName))
                }

                updatePieChart(pieEntries)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun updatePieChart(pieEntries: ArrayList<PieEntry>) {
        val pieDataSet = PieDataSet(pieEntries, "그래프: $selectedGraph")

        // 파이 차트 슬라이스에 대한 색상 설정
        pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        // 모든 텍스트 색상을 하얀색으로 설정
        val whiteColor = Color.rgb(255, 255, 255) // 흰색 RGB 값

        // 각 파이 조각에 표시할 텍스트 색상 설정
        pieDataSet.setValueTextColors(listOf(whiteColor)) // 값 텍스트 색상

        // 차트에서 라벨 및 범례 텍스트 색상 설정
        pieChart.setEntryLabelColor(whiteColor) // 카테고리 라벨 텍스트 색상
        pieChart.setCenterTextColor(whiteColor) // 중앙 텍스트 색상

        // 범례 텍스트 색상 설정
        val legend = pieChart.legend
        legend.textColor = whiteColor // 범례 텍스트 색상

        // 텍스트 크기 설정
        pieDataSet.setValueTextSize(16f) // 값 텍스트 크기 설정

        // 슬라이스에 대한 레이블 설정
        pieDataSet.setSliceSpace(2f) // 슬라이스 간 간격
        pieDataSet.setDrawIcons(false) // 아이콘 표시 여부 설정
        pieDataSet.setDrawValues(true) // 값 표시 여부 설정

        // 차트 설명 비활성화
        pieChart.description.isEnabled = false // 설명 숨기기

        // 그래프 데이터 설정
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.invalidate() // 차트 업데이트
    }

    // Firebase에서 특정 카테고리를 삭제하는 메소드
    private fun deleteCategoryFromFirebase(userId: String, graph: String, categoryName: String) {
        database.child("users").child(userId).child(graph).child(categoryName).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "카테고리가 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                editCategoryToDelete.text.clear()
                loadDataFromFirebase(userId, graph) // 삭제 후 데이터 로드
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "카테고리 삭제 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Firebase에서 전체 데이터를 삭제하는 메소드
    private fun deleteAllDataFromFirebase(userId: String, graph: String) {
        database.child("users").child(userId).child(graph).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "전체 데이터가 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                loadDataFromFirebase(userId, graph) // 전체 삭제 후 데이터 로드
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "전체 데이터 삭제 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
