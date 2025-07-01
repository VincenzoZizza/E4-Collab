<template>
  <div class="modal show d-block" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Delete Session {{ sessionId }}</h5>
          <button type="button" class="btn-close" @click="$emit('cancel')" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          Are you sure you want to delete this session?<br />This action cannot be undone.
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('cancel')">Cancel</button>
          <button type="button" class="btn btn-danger" @click="$emit('confirm')">Delete</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ sessionId: Number });
</script>

<style scoped>
.modal {
  background: rgba(0, 0, 0, 0.5);
}
</style>
