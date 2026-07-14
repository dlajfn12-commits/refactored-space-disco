package com.example.qrscanner.ui

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qrscanner.data.ScanRecord
import java.util.Calendar

class ScanViewModel : ViewModel() {

    private val _scanRecords = MutableLiveData<List<ScanRecord>>(emptyList())
    val scanRecords: LiveData<List<ScanRecord>> = _scanRecords

    private val _scanCount = MutableLiveData<Int>(0)
    val scanCount: LiveData<Int> = _scanCount

    private val _messageEvent = MutableLiveData<MessageEvent?>(null)
    val messageEvent: LiveData<MessageEvent?> = _messageEvent

    private val scanRecordsSet = mutableSetOf<String>()
    private var lastScanDate: String = getTodayDate()

    fun addScan(cellId: String): Boolean {
        val today = getTodayDate()
        
        // 날짜가 바뀌었으면 초기화
        if (today != lastScanDate) {
            scanRecordsSet.clear()
            _scanCount.value = 0
            lastScanDate = today
        }

        // 중복 체크
        if (scanRecordsSet.contains(cellId)) {
            _messageEvent.value = MessageEvent("이미 스캔된 QR입니다.", MessageType.DUPLICATE)
            return false
        }

        // 새 스캔 추가
        scanRecordsSet.add(cellId)
        val newRecord = ScanRecord(cellId)
        val currentList = _scanRecords.value?.toMutableList() ?: mutableListOf()
        currentList.add(newRecord)
        _scanRecords.value = currentList
        _scanCount.value = ((_scanCount.value) ?: 0) + 1
        _messageEvent.value = MessageEvent("✓ 스캔 성공", MessageType.SUCCESS)

        return true
    }

    fun getScanData(): List<ScanRecord> {
        return _scanRecords.value ?: emptyList()
    }

    fun resetMessage() {
        _messageEvent.value = null
    }

    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    data class MessageEvent(val message: String, val type: MessageType)

    enum class MessageType {
        SUCCESS, DUPLICATE, ERROR
    }
}
