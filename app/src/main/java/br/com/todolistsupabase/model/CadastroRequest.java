package br.com.todolistsupabase.model;

import com.google.gson.annotations.SerializedName;

public class CadastroRequest {

    private String email;
    private String password;

    @SerializedName("data")
    private UserData data;

    public CadastroRequest(
            String email,
            String password,
            String nome) {

        this.email = email;
        this.password = password;
        this.data = new UserData(nome);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNome() {
        if (data != null) {
            return data.getName();
        }

        return null;
    }

    public static class UserData {

        @SerializedName("name")
        private String name;

        public UserData(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}