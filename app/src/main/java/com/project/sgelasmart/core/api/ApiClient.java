package com.project.sgelasmart.core.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.project.sgelasmart.api.model.ModelLocalTime;
import com.project.sgelasmart.auth.AuthActivity;
import com.project.sgelasmart.core.auth.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://team-code-family-runtime-sgelasmartbackend.navho.shop/";
    private static Retrofit retrofit = null;
    private static Context appContext = null;
    private static SessionManager sessionManager = null;

    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        sessionManager = new SessionManager(appContext);
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request.Builder builder = originalRequest.newBuilder()
                                .addHeader("Accept", "application/json");

                        if (sessionManager != null && sessionManager.isLoggedIn()) {
                            builder.addHeader("Authorization", "Bearer " + sessionManager.getToken());
                        }

                        Response response = chain.proceed(builder.build());

                        if (response.code() == 401 && !originalRequest.url().encodedPath().contains("/api/auth/login")) {
                            if (sessionManager != null) {
                                sessionManager.clearSession();
                            }
                            if (appContext != null) {
                                Intent intent = new Intent(appContext, AuthActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                appContext.startActivity(intent);
                            }
                        }

                        return response;
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(OffsetDateTime.class, new TypeAdapter<OffsetDateTime>() {
                        @Override
                        public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                            }
                        }

                        @Override
                        public OffsetDateTime read(JsonReader in) throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            String str = in.nextString();
                            try {
                                return OffsetDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME);
                            } catch (Exception e) {
                                return LocalDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME).atOffset(java.time.ZoneOffset.UTC);
                            }
                        }
                    })
                    .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                        @Override
                        public void write(JsonWriter out, LocalDateTime value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            }
                        }

                        @Override
                        public LocalDateTime read(JsonReader in) throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            String str = in.nextString();
                            if (str.endsWith("Z") || str.contains("+")) {
                                return OffsetDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
                            }
                            return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                    })
                    .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                        @Override
                        public void write(JsonWriter out, LocalDate value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
                            }
                        }

                        @Override
                        public LocalDate read(JsonReader in) throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            return LocalDate.parse(in.nextString(), DateTimeFormatter.ISO_LOCAL_DATE);
                        }
                    })
                    .registerTypeAdapter(ModelLocalTime.class, new TypeAdapter<ModelLocalTime>() {
                        @Override
                        public void write(JsonWriter out, ModelLocalTime value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                int h = value.getHour() != null ? value.getHour() : 0;
                                int m = value.getMinute() != null ? value.getMinute() : 0;
                                int s = value.getSecond() != null ? value.getSecond() : 0;
                                out.value(String.format("%02d:%02d:%02d", h, m, s));
                            }
                        }

                        @Override
                        public ModelLocalTime read(JsonReader in) throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            if (in.peek() == JsonToken.STRING) {
                                String timeStr = in.nextString();
                                try {
                                    LocalTime parsed = LocalTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_TIME);
                                    ModelLocalTime model = new ModelLocalTime();
                                    model.setHour(parsed.getHour());
                                    model.setMinute(parsed.getMinute());
                                    model.setSecond(parsed.getSecond());
                                    return model;
                                } catch (Exception e) {
                                    return null;
                                }
                            }
                            if (in.peek() == JsonToken.BEGIN_OBJECT) {
                                in.beginObject();
                                ModelLocalTime model = new ModelLocalTime();
                                while (in.hasNext()) {
                                    String name = in.nextName();
                                    if ("hour".equals(name)) model.setHour(in.nextInt());
                                    else if ("minute".equals(name)) model.setMinute(in.nextInt());
                                    else if ("second".equals(name)) model.setSecond(in.nextInt());
                                    else in.skipValue();
                                }
                                in.endObject();
                                return model;
                            }
                            in.skipValue();
                            return null;
                        }
                    })
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static <S> S createService(Class<S> serviceClass) {
        return getClient().create(serviceClass);
    }
}
