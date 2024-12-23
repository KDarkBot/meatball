package com.example.realmeatball22222

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.realmeatball22222.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var pieChart: PieChart
    private lateinit var editCategory: EditText
    private lateinit var editPercent: EditText
    private lateinit var saveButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var database: DatabaseReference

    private var selectedGraph = "graph1" // 기본 선택된 그래프
    private var userId: String? = null // 사용자 ID (익명 사용자 고유 ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteman.setOnClickListener {
            val intent = Intent(this, delete_activity::class.java)
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
        editCategory = findViewById(R.id.editCategory)
        editPercent = findViewById(R.id.editPercent)
        saveButton = findViewById(R.id.saveButton)
        radioGroup = findViewById(R.id.radioGroup)

        // Firebase Database 초기화
        database = FirebaseDatabase.getInstance().reference

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

        // 저장 버튼 클릭 이벤트
        saveButton.setOnClickListener {
            val categoryName = editCategory.text.toString().trim()
            val percentageText = editPercent.text.toString().trim()

            if (categoryName.isEmpty() || percentageText.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val percentage = percentageText.toFloatOrNull()
            if (percentage == null || percentage <= 0) {
                Toast.makeText(this, "유효한 비율을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userId?.let { saveDataToFirebase(it, selectedGraph, categoryName, percentage) }
        }
    }

    override fun onResume() {
        super.onResume()

        // 로그인된 상태에서 데이터 로드
        userId?.let { loadDataFromFirebase(it, selectedGraph) }
    }

    // 데이터를 Firebase에 저장하는 메소드
    private fun saveDataToFirebase(userId: String, graph: String, categoryName: String, percentage: Float) {
        database.child("users").child(userId).child(graph).child(categoryName).setValue(percentage)
            .addOnSuccessListener {
                Toast.makeText(this, "데이터가 저장되었습니다!", Toast.LENGTH_SHORT).show()
                editCategory.text.clear()
                editPercent.text.clear()

                // 데이터 저장 후 다시 그래프를 갱신
                loadDataFromFirebase(userId, graph)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "데이터 저장 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Firebase에서 데이터를 불러오는 메소드 (차트 갱신)
    private fun loadDataFromFirebase(userId: String, graph: String) {
        database.child("users").child(userId).child(graph).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pieEntries = ArrayList<PieEntry>()

                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key ?: ""
                    val value = categorySnapshot.getValue(Float::class.java) ?: 0f
                    pieEntries.add(PieEntry(value, categoryName))
                }

                // 데이터가 변경된 후 차트를 갱신
                updatePieChart(pieEntries)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 차트 업데이트
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
}
