<template>
<div class="d-flex justify-content-center align-items-center min-vh-100">
    <div id='login-card' class='card p-4 shadow-lg d-flex flex-column' v-if="login">
        <img :src="collabVert" id='collab-logo'  alt="logo" />
        <h4 class='text-center mb-3'>Login</h4>
        <div v-if='param.error' id='error-message' class='alert alert-danger text-center'>
            Invalid username or password. <br>Please try again.
        </div>
        <div v-if='param.signup' id='success-message' class='alert alert-success text-center'>
            Signup successful. Please login.
        </div>
        <div class='flex-grow-1 d-flex flex-column'>
            <div class="input-group mb-3">
                <span class='input-group-text'><i class='bi bi-person-lock'></i></span>
                <input v-model="username" type='text' class='form-control' id='lusername' placeholder='Username' aria-label="Username" required>
            </div>
            <div class="input-group mb-3">
                <span class='input-group-text'><i class='bi bi-key'></i></span>
                <input v-model="password" type='password' class='form-control' id='lpassword' placeholder='Password' aria-label="Password" required>
            </div>
            <div class='d-flex justify-content-between mt-auto pt-3'>
                <button class='btn btn-secondary' @click="changeForm()">Sign Up</button>
                <button type='button' @click="sendLogin()" class='btn btn-primary'>Login</button>
            </div>
        </div>
    </div>





    <div id='signup-card' class='card p-4 shadow-lg d-flex flex-column' v-if="!login">
        <img :src="collabVert" id='collab-logo'  alt="logo" />
        <h4 class='text-center mb-3'>Signup</h4>
        <div id='error-message' class='alert alert-danger d-none text-center'>
            Invalid username or password. <br>Please try again.
        </div>
        <div class='needs-validation flex-grow-1 d-flex flex-column'>
            <div class="input-group mb-3 has-validation">
                <span class='input-group-text'><i class='bi bi-person-lock'></i></span>
                <input v-model ="username" type='text' class='form-control' id='susername' placeholder='Username' aria-label="Username"
                    pattern="^[a-zA-Z0-9_]{3,}$">
                <div class="invalid-feedback">
                    Username must be at least 3 characters long and can only contain letters, numbers, and underscores.
                </div>
            </div>
            <div class="input-group mb-3">
                <span class='input-group-text'><i class='bi bi-envelope'></i></span>
                <input v-model="email" type='email' class='form-control' id='semail' placeholder='Email' aria-label="Email">
            </div>
            <div class="input-group mb-3 has-validation">
                <span class='input-group-text'><i class='bi bi-key'></i></span>
                <input v-model="password" type='password' class='form-control' id='spassword' placeholder='Password' aria-label="Password"
                pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?=.*\d).{8,}$" required>
                <div class="invalid-feedback">
                    Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter,
                    one number, and one special character.
                </div>
            </div>
            <div class='d-flex justify-content-between mt-auto pt-3'>
                <button type='button' class='btn btn-secondary' @click="changeForm()">Login</button>
                <button type='button' class='btn btn-primary' @click="signup()" >Sign Up</button>
            </div>
        </div>
    </div>
</div>
<!--footer-->
</template>
<script setup>
import collabVert from "@/assets/collab-vertical.svg";
import { RouterLink } from 'vue-router'
import { ref } from 'vue';
import { loginRest, signUpRest } from '@/service/api.js';

import { useUserStore } from '@/stores/user.js';

import { useRouter } from 'vue-router'

const router = useRouter()

const userStore = useUserStore();

const param = ref({
    error: false,
    signup: false
});    

const login = ref(true);
const username = ref('');
const password = ref('');
const email = ref('');


function changeForm() {
    username.value = '';
    password.value = '';   
    email.value = '';
    login.value = !login.value;
}

function sendLogin() {
    loginRest(username.value, password.value ).then(response => {
        if (response.status === 200) {
            userStore.setUser(response.data.username, response.data.role);
            console.log(response.data);
            console.log("Login successful");
            router.push("/dashboard");
        } else {
            param.value.error = true;
        }
    }).catch(error => {
        console.error("Login failed:", error);
        param.value.error = true; // Show error message on failure
    })

    //403 forbidden al fallimento
}

function signup() {
    signUpRest(username.value, password.value, email.value ).then(response => {
        if (response.status === 200) {
            console.log("Signup successful");
        } else {
            param.value.error = true;
        }
    }).catch(error => {
        console.error("Signup failed:", error);
        param.value.error = true; // Show error message on failure
    })
}

//https://www.javaguides.net/2024/05/spring-boot-vuejs-user-registration-and-login-example.html per login
</script>