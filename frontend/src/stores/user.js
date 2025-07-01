import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  const username = ref("")
  const role = ref(0)
  const renderView = ref(false)

  function setRenderView(value)
  {
    renderView.value = value;
  }

  function setUser(newUsername, newRole) {
    username.value = newUsername
    role.value = newRole
  }

  return { username, role, renderView , setUser, setRenderView }
})
