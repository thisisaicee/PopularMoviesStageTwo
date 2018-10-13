package net.aicee.popularmoviesstagetwo.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse<apiResponse> {
    @SerializedName("results")
    public List<apiResponse> results;
}


