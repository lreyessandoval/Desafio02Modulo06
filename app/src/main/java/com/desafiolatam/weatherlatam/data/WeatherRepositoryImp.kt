package com.desafiolatam.weatherlatam.data

import android.util.Log
import com.desafiolatam.weatherlatam.data.local.WeatherDao
import com.desafiolatam.weatherlatam.data.remote.OpenWeatherService
import com.desafiolatam.weatherlatam.data.remote.RetrofitClient
import com.desafiolatam.weatherlatam.data.remote.ServiceResponse
import com.desafiolatam.weatherlatam.model.WeatherDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class WeatherRepositoryImp(
    private val weatherDao: WeatherDao
) : WeatherRepository {

    override suspend fun getRemoteWeatherData(): Flow<ServiceResponse<WeatherDto?>> {

        val data: MutableStateFlow<ServiceResponse<WeatherDto?>> =
            MutableStateFlow(ServiceResponse.Loading(true))

        val service = RetrofitClient.getInstance().create(OpenWeatherService::class.java)
        val response = service.getWeatherData(
            lat = -33.43107,
            lon = -70.64666,
            appid = OPEN_WEATHER_KEY
        )


        Log.d(TAG, "WeatherData1: ${response.body()}")
        
        when {
            response.isSuccessful -> data.value =
                ServiceResponse.Success(response.body()?.toWeatherDto())

            else -> {
                if (response.code() == 401) data.value = ServiceResponse.Error("Unauthorized")
                if (response.code() == 404) data.value = ServiceResponse.Error("Not found")
                if (response.code() == 500) data.value = ServiceResponse.Error("Internal Server Error")
                if (response.code() == 503) data.value = ServiceResponse.Error("Service Unavailable")
            }
        }
        return flowOf(data.value)
    }

    override suspend fun getWeatherData(): Flow<List<WeatherDto>?> =
        weatherDao.getWeatherData().map { entity -> entity?.let { entityListToDtoList(it) } }

    override suspend fun getWeatherDataById(id: Int): Flow<WeatherDto?> =
        weatherDao.getWeatherDataById(id).map { entity ->
            entity?.let { entityToDto(it) }
        }

    override suspend fun insertData(weatherDto: WeatherDto) =
        weatherDao.insertData(weatherDto.toEntity())

    override suspend fun clearAll() = weatherDao.clearAll()

    override suspend fun saveCityName(weatherDto: WeatherDto) =
        weatherDao.insertData(weatherDto.toEntity())
}