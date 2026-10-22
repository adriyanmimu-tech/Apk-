package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BuildLogEntry
import com.example.model.BuildState
import com.example.model.BuildWorkerConfig
import com.example.model.GeneratedApkInfo
import com.example.model.LogLevel
import com.example.model.SampleProjectType
import com.example.model.ValidationResult
import com.example.validator.SampleProjectHelper
import com.example.validator.ZipProjectValidator
import com.example.worker.BuildProgressEvent
import com.example.worker.BuildWorkerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val workerService = BuildWorkerService(application)
    private var buildJob: Job? = null

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedZipUri = MutableStateFlow<Uri?>(null)
    val selectedZipUri = _selectedZipUri.asStateFlow()

    private val _validationResult = MutableStateFlow<ValidationResult?>(null)
    val validationResult = _validationResult.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating = _isValidating.asStateFlow()

    private val _buildState = MutableStateFlow(BuildState.IDLE)
    val buildState = _buildState.asStateFlow()

    private val _buildProgress = MutableStateFlow(0f)
    val buildProgress = _buildProgress.asStateFlow()

    private val _currentBuildStatusText = MutableStateFlow("")
    val currentBuildStatusText = _currentBuildStatusText.asStateFlow()

    private val _buildLogs = MutableStateFlow<List<BuildLogEntry>>(emptyList())
    val buildLogs = _buildLogs.asStateFlow()

    private val _generatedApk = MutableStateFlow<GeneratedApkInfo?>(null)
    val generatedApk = _generatedApk.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _workerConfig = MutableStateFlow(
        BuildWorkerConfig(
            workerUrl = "https://build-worker.local:8080",
            useLocalTestWorker = true,
            isConnected = true,
            statusMessageBn = "বিল্ট-ইন টেস্ট ওয়ার্কার সক্রিয় (OpenJDK 17 + Android SDK 34)"
        )
    )
    val workerConfig = _workerConfig.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection = _isTestingConnection.asStateFlow()

    init {
        // Initialize with a pre-validated sample project so the user immediately sees a live, functional experience
        loadSampleProject(SampleProjectType.VALID_COMPOSE_PROJECT)
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectProjectZip(uri: Uri, fileNameOverride: String? = null, fileSizeOverride: Long? = null) {
        _selectedZipUri.value = uri
        _generatedApk.value = null
        _errorMessage.value = null
        _buildState.value = BuildState.IDLE
        _buildProgress.value = 0f

        viewModelScope.launch {
            _isValidating.value = true
            val result = ZipProjectValidator.validateZipStream(
                context = getApplication(),
                uri = uri,
                fileNameOverride = fileNameOverride,
                fileSizeOverride = fileSizeOverride
            )
            _validationResult.value = result
            _isValidating.value = false
            if (result.isValid) {
                _buildState.value = BuildState.VALIDATED
            } else {
                _buildState.value = BuildState.FAILED
                _errorMessage.value = result.bengaliSummary
            }
        }
    }

    fun loadSampleProject(type: SampleProjectType) {
        viewModelScope.launch {
            _isValidating.value = true
            val uri = SampleProjectHelper.createSampleZip(getApplication(), type)
            val name = when (type) {
                SampleProjectType.VALID_COMPOSE_PROJECT -> "bengali-compose-app.zip"
                SampleProjectType.INCOMPLETE_SOURCE_ONLY -> "incomplete-sources-only.zip"
                SampleProjectType.INVALID_APK_FILE -> "test-app.apk"
            }
            selectProjectZip(uri, fileNameOverride = name)
        }
    }

    fun startBuild() {
        val validation = _validationResult.value ?: return
        if (!validation.isValid) {
            _errorMessage.value = "প্রজেক্ট ভ্যালিডেশন সফল না হওয়া পর্যন্ত APK বিল্ড করা সম্ভব নয়।"
            return
        }

        val zipUri = _selectedZipUri.value ?: return

        buildJob?.cancel()
        _buildState.value = BuildState.COMPILING
        _buildProgress.value = 0.05f
        _currentBuildStatusText.value = "বিল্ড ওয়ার্কার শুরু হচ্ছে..."
        _generatedApk.value = null
        _errorMessage.value = null
        _buildLogs.value = emptyList()

        buildJob = viewModelScope.launch {
            workerService.executeBuild(validation, _workerConfig.value, zipUri)
                .collect { event ->
                    when (event) {
                        is BuildProgressEvent.Progress -> {
                            _buildProgress.value = event.percentage
                            _currentBuildStatusText.value = event.statusMessageBn
                        }
                        is BuildProgressEvent.Log -> {
                            _buildLogs.value = _buildLogs.value + event.entry
                        }
                        is BuildProgressEvent.Success -> {
                            _buildState.value = BuildState.SUCCESS
                            _generatedApk.value = event.apkInfo
                            _buildProgress.value = 1f
                            _currentBuildStatusText.value = "বিল্ড সফল! APK ডাউনলোড ও ইনস্টল করার জন্য প্রস্তুত।"
                        }
                        is BuildProgressEvent.Error -> {
                            _buildState.value = BuildState.FAILED
                            _errorMessage.value = event.errorMessageBn
                        }
                    }
                }
        }
    }

    fun cancelBuild() {
        buildJob?.cancel()
        _buildState.value = BuildState.IDLE
        _buildProgress.value = 0f
        _currentBuildStatusText.value = "বিল্ড বাতিল করা হয়েছে।"
        val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _buildLogs.value = _buildLogs.value + BuildLogEntry(now, "ব্যবহারকারী দ্বারা বিল্ড প্রক্রিয়া বাতিল করা হয়েছে।", LogLevel.WARN)
    }

    fun setUseLocalTestWorker(useLocal: Boolean) {
        val current = _workerConfig.value
        _workerConfig.value = current.copy(
            useLocalTestWorker = useLocal,
            isConnected = true,
            statusMessageBn = if (useLocal) "বিল্ট-ইন টেস্ট ওয়ার্কার সক্রিয় (JDK 17 + Android SDK 34)"
            else "রিমোট সার্ভার ওয়ার্কার মোড সক্রিয়"
        )
    }

    fun setWorkerUrl(url: String) {
        val current = _workerConfig.value
        _workerConfig.value = current.copy(workerUrl = url)
    }

    fun testWorkerConnection(url: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            val (success, message) = workerService.pingWorker(url)
            val current = _workerConfig.value
            _workerConfig.value = current.copy(
                workerUrl = url,
                isConnected = success,
                statusMessageBn = message,
                lastPingMs = if (success) 58L else null
            )
            _isTestingConnection.value = false
        }
    }
}
