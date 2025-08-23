<template>
  <a-layout-footer class="global-footer">
    <div class="footer-container">
      <div class="footer-content">
        <span class="copyright-text">
          © 2024 
          <a 
            href="https://github.com/hateStudyy" 
            target="_blank" 
            rel="noopener noreferrer"
            class="github-link"
          >
            coldwind-official
          </a>
          . All rights reserved.
        </span>
      </div>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的逻辑
</script>

<style scoped>
.global-footer {
  background: #f0f2f5;
  border-top: 1px solid #e8e8e8;
  padding: 0;
  height: 70px;
  line-height: 70px;
  text-align: center;
  position: relative;
  bottom: 0;
  width: 100%;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.copyright-text {
  color: #666;
  font-size: 14px;
}

.github-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.github-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-container {
    padding: 0 16px;
  }
  
  .copyright-text {
    font-size: 12px;
  }
}
</style>
