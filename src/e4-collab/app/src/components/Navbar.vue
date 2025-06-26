<template>
<nav id='navbar' class='navbar navbar-expand-sm navbar-dark'>
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
                    <a class='nav-link' aria-current='page' href='/'>Dashboard</a>
                </li>
                <li class='nav-item'>
                    <a class='nav-link' aria-current='page' href='/sessions'>Sessions</a>
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

import collabLogo from "@/assets/collab_bianco.svg";
        /*const currentUri = window.location.pathname;
        const navbar = document.getElementById('navbar');
        const navbarLinks = Array.from(navbar.getElementsByClassName('nav-link'));
        const currentNavLink = navbarLinks.find(item => URL.parse(item.href).pathname === currentUri);
        currentNavLink.classList.add('active');*/
</script>