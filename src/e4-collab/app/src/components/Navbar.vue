<template>
<nav id='navbar' class='navbar navbar-expand-sm navbar-dark' style="margin: 0;">
    <div class='container-fluid'>
        <a class='navbar-brand' href='/'>
            <img :src='collabLogo' alt='logo'>
        </a>
        <div class='order-sm-last d-inline-flex gap-3'>
            <!--<div th:replace='~{fragments/common::user-dropdown}'></div>-->
            <button class='navbar-toggler' type='button' data-bs-toggle='collapse'
                data-bs-target='#navbarSupportedContent' aria-controls='navbarSupportedContent' aria-expanded='false'
                aria-label='Toggle navigation'>
                <span class='navbar-toggler-icon'></span>
            </button>
        </div>
        <div class='collapse navbar-collapse' id='navbarSupportedContent'>
            <ul class='navbar-nav me-auto mb-2 mb-lg-0'>
                <li class='nav-item'>
                    <RouterLink class='nav-link' to="/dashboard">Dashboard</RouterLink>
                </li>
                <li class='nav-item'>
                   <RouterLink class='nav-link' to='/sessions'>Sessions</RouterLink>
                </li>
                <li class='nav-item'>
                   <!-- th:if="${session?.SPRING_SECURITY_CONTEXT?.authentication?.authorities != null && (#lists.contains(session.SPRING_SECURITY_CONTEXT.authentication.authorities.![authority], 'ROLE_EDITOR') || #lists.contains(session.SPRING_SECURITY_CONTEXT.authentication.authorities.![authority], 'ROLE_ADMIN'))}">-->
                    <a class='nav-link' href='/users'>Users</a>
                </li>
            </ul>
        </div>
    </div>
</nav>
</template>
<script setup>

import collabLogo from "@/assets/collab_bianco.svg";7
import { onMounted, defineEmits } from 'vue'
import { checkSession } from '@/service/api.js';
import { useUserStore } from '@/stores/user.js';

import { useRouter, RouterLink } from 'vue-router'

const router = useRouter()

const userStore = useUserStore();

const emit = defineEmits(['isReady']);

onMounted(() => {
    userStore.setRenderView(false)
  checkSession().then(response => {
        if (response.status === 200) {
            userStore.setUser(response.data.username, response.data.role);
            console.log("Login successful");
            userStore.setRenderView(true)
            router.push("/dashboard");
        } else {
            userStore.setUser("", 0);
            userStore.setRenderView(true)
            alert("Sessione scaduta! Rieffettuare il login.")
            router.push("/");
        }
    }).catch(error => {
        console.error("Login failed:", error);
        userStore.setRenderView(true)
        alert("Errore interno.")
        router.push("/");
    })
})
        /*const currentUri = window.location.pathname;
        const navbar = document.getElementById('navbar');
        const navbarLinks = Array.from(navbar.getElementsByClassName('nav-link'));
        const currentNavLink = navbarLinks.find(item => URL.parse(item.href).pathname === currentUri);
        currentNavLink.classList.add('active');*/
</script>