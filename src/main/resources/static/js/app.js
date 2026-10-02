/**
 * Student Attendance Management System - Frontend Application Logic
 */

// Global State
const state = {
  currentUser: null,
  courses: [],
  students: [],
  activeTab: 'admin-dashboard',
  sheetData: [],
  selectedReportCourse: '',
  selectedReportStudent: ''
};

// API Base URL
const API = {
  login: '/api/auth/login',
  students: '/api/students',
  courses: '/api/courses',
  attendanceMark: '/api/attendance/mark',
  attendanceSheet: '/api/attendance/sheet',
  attendanceSummary: (studentId) => `/api/attendance/student/${studentId}/summary`,
  attendanceHistory: (studentId) => `/api/attendance/student/${studentId}/history`,
  attendanceReport: '/api/attendance/report',
  attendanceCsv: '/api/attendance/report/export-csv',
  attendanceShortage: '/api/attendance/shortage'
};

// ===================================================================
// Initialization & Authentication
// ===================================================================

document.addEventListener('DOMContentLoaded', () => {
  initDatePickers();
  checkPersistedAuth();
  bindEvents();
});

function initDatePickers() {
  const today = new Date().toISOString().split('T')[0];
  const dateInput = document.getElementById('attendanceDate');
  if (dateInput) dateInput.value = today;

  const rStart = document.getElementById('reportStartDate');
  const rEnd = document.getElementById('reportEndDate');
  if (rStart && rEnd) {
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    rStart.value = thirtyDaysAgo.toISOString().split('T')[0];
    rEnd.value = today;
  }
}

function checkPersistedAuth() {
  const saved = localStorage.getItem('attendance_user');
  if (saved) {
    try {
      state.currentUser = JSON.parse(saved);
      showAppView();
    } catch (e) {
      localStorage.removeItem('attendance_user');
      showLoginView();
    }
  } else {
    showLoginView();
  }
}

function showLoginView() {
  document.getElementById('authSection').style.display = 'flex';
  document.getElementById('appSection').style.display = 'none';
}

function showAppView() {
  document.getElementById('authSection').style.display = 'none';
  document.getElementById('appSection').style.display = 'flex';

  const user = state.currentUser;
  document.getElementById('navUserName').textContent = user.fullName || user.username;
  document.getElementById('navUserRole').textContent = user.role;

  // Toggle role-specific navigation links
  const adminLinks = document.querySelectorAll('.admin-nav-item');
  const studentLinks = document.querySelectorAll('.student-nav-item');

  if (user.role === 'ADMIN') {
    adminLinks.forEach(el => el.style.display = 'flex');
    studentLinks.forEach(el => el.style.display = 'none');
    loadCourses().then(() => {
      switchTab('admin-dashboard');
      loadAdminDashboard();
    });
  } else {
    adminLinks.forEach(el => el.style.display = 'none');
    studentLinks.forEach(el => el.style.display = 'flex');
    switchTab('student-dashboard');
    loadStudentDashboard();
  }
}

function bindEvents() {
  // Login form
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
  }

  // Logout button
  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', handleLogout);
  }

  // Sidebar navigation clicks
  document.querySelectorAll('.nav-link[data-tab]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const tabId = btn.getAttribute('data-tab');
      switchTab(tabId);
    });
  });

  // Add student form submit
  const addStudentForm = document.getElementById('addStudentForm');
  if (addStudentForm) {
    addStudentForm.addEventListener('submit', handleAddStudent);
  }
}

// ===================================================================
// Authentication Handlers
// ===================================================================

async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value.trim();
  const errorEl = document.getElementById('loginError');

  errorEl.style.display = 'none';

  if (!username || !password) {
    errorEl.textContent = 'Please enter both username and password.';
    errorEl.style.display = 'block';
    return;
  }

  try {
    const res = await fetch(API.login, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const data = await res.json();
    if (res.ok && data.success) {
      state.currentUser = data;
      localStorage.setItem('attendance_user', JSON.stringify(data));
      showToast('Logged in successfully!', 'success');
      showAppView();
    } else {
      errorEl.textContent = data.message || 'Invalid username or password.';
      errorEl.style.display = 'block';
    }
  } catch (err) {
    errorEl.textContent = 'Unable to connect to server. Please ensure backend is running.';
    errorEl.style.display = 'block';
  }
}

function handleLogout() {
  state.currentUser = null;
  localStorage.removeItem('attendance_user');
  showToast('Logged out successfully.', 'info');
  showLoginView();
}

function fillDemo(username, password) {
  document.getElementById('loginUsername').value = username;
  document.getElementById('loginPassword').value = password;
}

// ===================================================================
// Tab Navigation
// ===================================================================

function switchTab(tabId) {
  state.activeTab = tabId;

  // Update nav active classes
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.toggle('active', link.getAttribute('data-tab') === tabId);
  });

  // Update tab pane active classes
  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.toggle('active', pane.id === tabId);
  });

  // Trigger tab-specific loaders
  switch (tabId) {
    case 'admin-dashboard':
      loadAdminDashboard();
      break;
    case 'admin-students':
      loadStudentsList();
      break;
    case 'admin-mark':
      initMarkAttendanceForm();
      break;
    case 'admin-analytics':
      loadAnalyticsData();
      break;
    case 'admin-reports':
      initReportsView();
      break;
    case 'student-dashboard':
      loadStudentDashboard();
      break;
    case 'student-history':
      loadStudentHistoryView();
      break;
  }
}

// ===================================================================
// Shared Data Loaders
// ===================================================================

async function loadCourses() {
  try {
    const res = await fetch(API.courses);
    if (res.ok) {
      state.courses = await res.json();
      populateCourseSelects();
    }
  } catch (e) {
    console.error('Failed to load courses', e);
  }
}

function populateCourseSelects() {
  const selects = ['attendanceCourse', 'reportCourse', 'analyticsCourse', 'historyCourseFilter'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    const currentVal = el.value;
    el.innerHTML = (id === 'attendanceCourse') ? '<option value="">-- Select Course / Subject --</option>' : '<option value="">All Courses / Subjects</option>';
    state.courses.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.id;
      opt.textContent = `${c.courseCode} - ${c.courseName}`;
      el.appendChild(opt);
    });
    if (currentVal) el.value = currentVal;
  });
}

// ===================================================================
// Admin: Dashboard
// ===================================================================

async function loadAdminDashboard() {
  try {
    const [studentsRes, coursesRes, shortageRes] = await Promise.all([
      fetch(API.students),
      fetch(API.courses),
      fetch(API.attendanceShortage + '?threshold=75.0')
    ]);

    const students = await studentsRes.json();
    const courses = await coursesRes.json();
    const shortage = await shortageRes.json();

    document.getElementById('statTotalStudents').textContent = students.length;
    document.getElementById('statTotalCourses').textContent = courses.length;
    document.getElementById('statShortageCount').textContent = shortage.length;

    // Fetch sample overall report to count records
    const reportRes = await fetch(API.attendanceReport);
    if (reportRes.ok) {
      const report = await reportRes.json();
      document.getElementById('statTotalRecords').textContent = report.totalRecords || 0;
      document.getElementById('statAverageAttendance').textContent = (report.overallPercentage || 0) + '%';
    }

    renderRecentActivityTable();
  } catch (e) {
    console.error('Error loading dashboard stats', e);
  }
}

async function renderRecentActivityTable() {
  try {
    const res = await fetch(API.attendanceReport);
    if (!res.ok) return;
    const report = await res.json();
    const tbody = document.getElementById('recentAttendanceTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';
    const recent = (report.records || []).slice(0, 8);

    if (recent.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="text-align: center; padding: 20px;">No attendance records found yet.</td></tr>';
      return;
    }

    recent.forEach(r => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>${r.attendanceDate}</strong></td>
        <td><span class="badge badge-info">${r.courseCode}</span></td>
        <td>${r.rollNumber} - ${r.studentName}</td>
        <td><span class="badge badge-${r.status.toLowerCase()}">${r.status}</span></td>
        <td class="text-muted">${r.remarks || '-'}</td>
      `;
      tbody.appendChild(tr);
    });
  } catch (e) {
    console.error(e);
  }
}

// ===================================================================
// Admin: Students Management
// ===================================================================

async function loadStudentsList() {
  try {
    const res = await fetch(API.students);
    if (res.ok) {
      state.students = await res.json();
      renderStudentsTable(state.students);
    }
  } catch (e) {
    showToast('Failed to load students list.', 'error');
  }
}

function renderStudentsTable(students) {
  const tbody = document.getElementById('studentsTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  if (students.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 24px;" class="text-muted">No students found.</td></tr>';
    return;
  }

  students.forEach((s, idx) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${idx + 1}</td>
      <td><strong>${s.rollNumber}</strong></td>
      <td>${s.fullName}</td>
      <td>${s.department}</td>
      <td>Sem ${s.semester} (${s.section})</td>
      <td>${s.email}</td>
      <td style="text-align: right; white-space: nowrap;">
        <button class="btn btn-secondary btn-sm" onclick="viewStudentSummaryModal(${s.id})">📊 Report</button>
        <button class="btn btn-danger btn-sm" onclick="deleteStudent(${s.id}, '${s.rollNumber}')">🗑️</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function filterStudentsTable() {
  const query = document.getElementById('studentSearchInput').value.toLowerCase().trim();
  const dept = document.getElementById('studentDeptFilter').value;

  const filtered = state.students.filter(s => {
    const matchQuery = s.rollNumber.toLowerCase().includes(query) ||
                       s.fullName.toLowerCase().includes(query) ||
                       s.email.toLowerCase().includes(query);
    const matchDept = !dept || s.department === dept;
    return matchQuery && matchDept;
  });

  renderStudentsTable(filtered);
}

function openAddStudentModal() {
  document.getElementById('addStudentForm').reset();
  document.getElementById('addStudentModal').classList.add('active');
}

function closeAddStudentModal() {
  document.getElementById('addStudentModal').classList.remove('active');
}

async function handleAddStudent(e) {
  e.preventDefault();
  const btn = document.getElementById('saveStudentBtn');
  btn.disabled = true;
  btn.textContent = 'Saving...';

  const studentData = {
    rollNumber: document.getElementById('newStudentRoll').value.trim(),
    fullName: document.getElementById('newStudentName').value.trim(),
    email: document.getElementById('newStudentEmail').value.trim(),
    phone: document.getElementById('newStudentPhone').value.trim(),
    department: document.getElementById('newStudentDept').value,
    semester: parseInt(document.getElementById('newStudentSem').value),
    section: document.getElementById('newStudentSec').value.trim() || 'A',
    password: document.getElementById('newStudentPass').value.trim() || 'student123'
  };

  try {
    const res = await fetch(API.students, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(studentData)
    });

    const data = await res.json();
    if (res.ok) {
      showToast(`Student ${data.rollNumber} added successfully!`, 'success');
      closeAddStudentModal();
      loadStudentsList();
    } else {
      showToast(data.error || 'Failed to add student.', 'error');
    }
  } catch (err) {
    showToast('Server error while saving student.', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Save Student';
  }
}

async function deleteStudent(id, rollNumber) {
  if (!confirm(`Are you sure you want to delete student ${rollNumber}? This will also delete their attendance records.`)) {
    return;
  }

  try {
    const res = await fetch(`${API.students}/${id}`, { method: 'DELETE' });
    if (res.ok) {
      showToast(`Student ${rollNumber} deleted.`, 'success');
      loadStudentsList();
    } else {
      showToast('Failed to delete student.', 'error');
    }
  } catch (e) {
    showToast('Server error while deleting student.', 'error');
  }
}

async function viewStudentSummaryModal(studentId) {
  try {
    const res = await fetch(API.attendanceSummary(studentId));
    if (!res.ok) return;
    const summary = await res.json();

    document.getElementById('modalStudentSummaryName').textContent = `${summary.studentName} (${summary.rollNumber})`;
    document.getElementById('modalStudentSummaryPct').textContent = `${summary.attendancePercentage}%`;
    document.getElementById('modalStudentSummaryTotal').textContent = summary.totalClasses;
    document.getElementById('modalStudentSummaryAttended').textContent = summary.attendedClasses;
    document.getElementById('modalStudentSummaryAbsent').textContent = summary.absentClasses;

    const tbody = document.getElementById('modalStudentSubjectsBody');
    tbody.innerHTML = '';
    (summary.subjectAttendance || []).forEach(sub => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>${sub.courseCode}</strong></td>
        <td>${sub.courseName}</td>
        <td>${sub.attendedClasses} / ${sub.totalClasses}</td>
        <td><strong>${sub.percentage}%</strong></td>
        <td><span class="badge badge-${sub.statusBadge.toLowerCase()}">${sub.statusBadge}</span></td>
      `;
      tbody.appendChild(tr);
    });

    document.getElementById('studentSummaryModal').classList.add('active');
  } catch (e) {
    showToast('Failed to load student summary.', 'error');
  }
}

function closeStudentSummaryModal() {
  document.getElementById('studentSummaryModal').classList.remove('active');
}

// ===================================================================
// Admin: Mark Attendance
// ===================================================================

function initMarkAttendanceForm() {
  populateCourseSelects();
  const sheetContainer = document.getElementById('attendanceSheetContainer');
  if (sheetContainer) sheetContainer.style.display = 'none';
}

async function loadAttendanceSheet() {
  const courseId = document.getElementById('attendanceCourse').value;
  const date = document.getElementById('attendanceDate').value;

  if (!courseId) {
    showToast('Please select a course / subject first.', 'error');
    return;
  }
  if (!date) {
    showToast('Please select an attendance date.', 'error');
    return;
  }

  const loadBtn = document.getElementById('loadSheetBtn');
  loadBtn.disabled = true;
  loadBtn.textContent = 'Loading...';

  try {
    const res = await fetch(`${API.attendanceSheet}?courseId=${courseId}&date=${date}`);
    if (res.ok) {
      state.sheetData = await res.json();
      renderAttendanceSheet();
      document.getElementById('attendanceSheetContainer').style.display = 'block';
    } else {
      showToast('Could not load class sheet.', 'error');
    }
  } catch (e) {
    showToast('Server error loading attendance sheet.', 'error');
  } finally {
    loadBtn.disabled = false;
    loadBtn.textContent = 'Load Class Sheet';
  }
}

function renderAttendanceSheet() {
  const tbody = document.getElementById('attendanceSheetBody');
  tbody.innerHTML = '';

  if (state.sheetData.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 24px;" class="text-muted">No students enrolled in this course.</td></tr>';
    return;
  }

  state.sheetData.forEach((row, idx) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${idx + 1}</td>
      <td><strong>${row.rollNumber}</strong></td>
      <td>${row.studentName} <span class="text-muted">(${row.section})</span></td>
      <td>
        <div class="attendance-toggle-group" data-student-id="${row.studentId}">
          <button type="button" class="attendance-toggle-btn present ${row.status === 'PRESENT' ? 'active' : ''}" onclick="setRowStatus(${row.studentId}, 'PRESENT')">Present</button>
          <button type="button" class="attendance-toggle-btn absent ${row.status === 'ABSENT' ? 'active' : ''}" onclick="setRowStatus(${row.studentId}, 'ABSENT')">Absent</button>
          <button type="button" class="attendance-toggle-btn late ${row.status === 'LATE' ? 'active' : ''}" onclick="setRowStatus(${row.studentId}, 'LATE')">Late</button>
          <button type="button" class="attendance-toggle-btn excused ${row.status === 'EXCUSED' ? 'active' : ''}" onclick="setRowStatus(${row.studentId}, 'EXCUSED')">Excused</button>
        </div>
      </td>
      <td>
        <input type="text" class="remark-input" id="remark-${row.studentId}" value="${row.remarks || ''}" placeholder="Optional remark (e.g. sick, late)">
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function setRowStatus(studentId, status) {
  const item = state.sheetData.find(s => s.studentId === studentId);
  if (item) item.status = status;

  const group = document.querySelector(`.attendance-toggle-group[data-student-id="${studentId}"]`);
  if (group) {
    group.querySelectorAll('.attendance-toggle-btn').forEach(btn => {
      btn.classList.toggle('active', btn.classList.contains(status.toLowerCase()));
    });
  }
}

function markAll(status) {
  state.sheetData.forEach(item => {
    item.status = status;
    setRowStatus(item.studentId, status);
  });
  showToast(`Marked all as ${status}`, 'info');
}

async function saveAttendanceSheet() {
  const courseId = parseInt(document.getElementById('attendanceCourse').value);
  const attendanceDate = document.getElementById('attendanceDate').value;
  const saveBtn = document.getElementById('saveAttendanceBtn');

  saveBtn.disabled = true;
  saveBtn.textContent = 'Saving Attendance...';

  const entries = state.sheetData.map(item => {
    const remarkEl = document.getElementById(`remark-${item.studentId}`);
    return {
      studentId: item.studentId,
      status: item.status,
      remarks: remarkEl ? remarkEl.value.trim() : ''
    };
  });

  try {
    const res = await fetch(API.attendanceMark, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ courseId, attendanceDate, entries })
    });

    const data = await res.json();
    if (res.ok) {
      showToast(`Success! Attendance recorded for ${data.count} students.`, 'success');
      loadAttendanceSheet();
    } else {
      showToast(data.error || 'Failed to save attendance.', 'error');
    }
  } catch (e) {
    showToast('Server error while saving attendance.', 'error');
  } finally {
    saveBtn.disabled = false;
    saveBtn.textContent = '💾 Save Attendance';
  }
}

// ===================================================================
// Admin: Attendance Analytics & Defaulters
// ===================================================================

async function loadAnalyticsData() {
  populateCourseSelects();
  const threshold = document.getElementById('analyticsThreshold').value || 75.0;
  const courseId = document.getElementById('analyticsCourse').value;

  let url = `${API.attendanceShortage}?threshold=${threshold}`;
  if (courseId) url += `&courseId=${courseId}`;

  try {
    const res = await fetch(url);
    if (res.ok) {
      const list = await res.json();
      renderAnalyticsTable(list);
    }
  } catch (e) {
    console.error('Failed to load analytics', e);
  }
}

function renderAnalyticsTable(list) {
  const tbody = document.getElementById('analyticsTableBody');
  tbody.innerHTML = '';

  document.getElementById('defaulterCountBadge').textContent = `${list.length} Defaulters Found`;

  if (list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 24px;" class="text-success">🎉 No students currently below threshold!</td></tr>';
    return;
  }

  list.forEach((s, idx) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${idx + 1}</td>
      <td><strong>${s.rollNumber}</strong></td>
      <td>${s.studentName}</td>
      <td>${s.department} (Sem ${s.semester})</td>
      <td>${s.attendedClasses} / ${s.totalClasses}</td>
      <td>
        <div style="display: flex; align-items: center; gap: 8px;">
          <div style="flex: 1; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden;">
            <div style="width: ${s.attendancePercentage}%; height: 100%; background: ${s.attendancePercentage < 65 ? '#ef4444' : '#f59e0b'};"></div>
          </div>
          <strong>${s.attendancePercentage}%</strong>
        </div>
      </td>
      <td>
        <span class="badge badge-${s.statusBadge.toLowerCase()}">${s.statusBadge}</span>
        ${s.neededClasses > 0 ? `<small class="text-danger" style="display:block; margin-top:2px;">Need +${s.neededClasses} classes</small>` : ''}
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// ===================================================================
// Admin: Reports Generation
// ===================================================================

function initReportsView() {
  populateCourseSelects();
  populateReportStudentsDropdown();
  generateReport();
}

async function populateReportStudentsDropdown() {
  const el = document.getElementById('reportStudent');
  if (!el) return;
  try {
    const res = await fetch(API.students);
    if (res.ok) {
      const students = await res.json();
      el.innerHTML = '<option value="">All Students</option>';
      students.forEach(s => {
        const opt = document.createElement('option');
        opt.value = s.id;
        opt.textContent = `${s.rollNumber} - ${s.fullName}`;
        el.appendChild(opt);
      });
    }
  } catch (e) {
    console.error(e);
  }
}

async function generateReport() {
  const courseId = document.getElementById('reportCourse').value;
  const studentId = document.getElementById('reportStudent').value;
  const startDate = document.getElementById('reportStartDate').value;
  const endDate = document.getElementById('reportEndDate').value;

  let query = [];
  if (courseId) query.push(`courseId=${courseId}`);
  if (studentId) query.push(`studentId=${studentId}`);
  if (startDate) query.push(`startDate=${startDate}`);
  if (endDate) query.push(`endDate=${endDate}`);

  const queryString = query.length ? `?${query.join('&')}` : '';

  try {
    const res = await fetch(`${API.attendanceReport}${queryString}`);
    if (res.ok) {
      const report = await res.json();
      renderReportOutput(report);
    }
  } catch (e) {
    showToast('Failed to generate attendance report.', 'error');
  }
}

function renderReportOutput(report) {
  document.getElementById('reportTotalClasses').textContent = report.totalRecords;
  document.getElementById('reportTotalPresent').textContent = report.totalPresent;
  document.getElementById('reportTotalAbsent').textContent = report.totalAbsent;
  document.getElementById('reportOverallPct').textContent = `${report.overallPercentage}%`;

  const tbody = document.getElementById('reportTableBody');
  tbody.innerHTML = '';

  if (report.records.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 24px;" class="text-muted">No records match the selected filters.</td></tr>';
    return;
  }

  report.records.forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${r.attendanceDate}</strong></td>
      <td><span class="badge badge-info">${r.courseCode}</span> ${r.courseName}</td>
      <td>${r.rollNumber}</td>
      <td>${r.studentName}</td>
      <td><span class="badge badge-${r.status.toLowerCase()}">${r.status}</span></td>
      <td class="text-muted">${r.remarks || '-'}</td>
    `;
    tbody.appendChild(tr);
  });
}

function exportCsv() {
  const courseId = document.getElementById('reportCourse').value;
  const studentId = document.getElementById('reportStudent').value;
  const startDate = document.getElementById('reportStartDate').value;
  const endDate = document.getElementById('reportEndDate').value;

  let query = [];
  if (courseId) query.push(`courseId=${courseId}`);
  if (studentId) query.push(`studentId=${studentId}`);
  if (startDate) query.push(`startDate=${startDate}`);
  if (endDate) query.push(`endDate=${endDate}`);

  const queryString = query.length ? `?${query.join('&')}` : '';
  window.open(`${API.attendanceCsv}${queryString}`, '_blank');
}

// ===================================================================
// Student Portal: Dashboard & Features
// ===================================================================

async function loadStudentDashboard() {
  const user = state.currentUser;
  if (!user || !user.studentId) return;

  try {
    const res = await fetch(API.attendanceSummary(user.studentId));
    if (!res.ok) return;
    const summary = await res.json();

    // Populate student hero banner
    document.getElementById('studentHeroName').textContent = summary.studentName;
    document.getElementById('studentHeroRoll').textContent = `Roll: ${summary.rollNumber}`;
    document.getElementById('studentHeroDept').textContent = `${summary.department} - Sem ${summary.semester}`;

    // Animate radial percentage gauge
    const pct = summary.attendancePercentage;
    const circle = document.getElementById('studentRadialCircle');
    const text = document.getElementById('studentRadialText');

    text.textContent = `${pct}%`;
    const circumference = 251.2;
    const offset = circumference - (pct / 100) * circumference;
    circle.style.strokeDashoffset = offset;

    if (pct >= 75) {
      circle.style.stroke = '#10b981'; // Green
    } else if (pct >= 65) {
      circle.style.stroke = '#f59e0b'; // Amber
    } else {
      circle.style.stroke = '#ef4444'; // Red
    }

    // Attendance Advisor Alert
    const advisor = document.getElementById('studentAdvisorBox');
    if (pct >= 75) {
      advisor.className = 'advisor-alert good';
      advisor.innerHTML = `<strong>✨ Good Standing:</strong> Your attendance is safe at <strong>${pct}%</strong>. You can safely miss <strong>${summary.canBunkClasses}</strong> class(es) without falling below the 75% requirement.`;
    } else {
      advisor.className = 'advisor-alert critical';
      advisor.innerHTML = `<strong>⚠️ Attendance Shortage:</strong> Your attendance is currently <strong>${pct}%</strong> (below 75%). You must attend the next <strong>${summary.neededClasses}</strong> consecutive classes to regain good standing!`;
    }

    // Populate stats
    document.getElementById('studentTotalClasses').textContent = summary.totalClasses;
    document.getElementById('studentAttendedClasses').textContent = summary.attendedClasses;
    document.getElementById('studentAbsentClasses').textContent = summary.absentClasses;

    // Render Subject-Wise Cards
    renderStudentSubjectCards(summary.subjectAttendance || []);
  } catch (e) {
    console.error('Error loading student dashboard', e);
  }
}

function renderStudentSubjectCards(subjects) {
  const container = document.getElementById('studentSubjectCards');
  container.innerHTML = '';

  if (subjects.length === 0) {
    container.innerHTML = '<p class="text-muted">No enrolled subjects found.</p>';
    return;
  }

  subjects.forEach(sub => {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.marginBottom = '16px';
    const colorClass = sub.percentage >= 75 ? 'success' : (sub.percentage >= 65 ? 'warning' : 'danger');

    card.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
        <div>
          <span class="badge badge-info" style="margin-bottom: 4px;">${sub.courseCode}</span>
          <h4 style="font-size: 1rem; margin: 0;">${sub.courseName}</h4>
        </div>
        <span class="badge badge-${sub.statusBadge.toLowerCase()}">${sub.percentage}%</span>
      </div>
      <div style="margin-bottom: 8px;">
        <div style="display: flex; justify-content: space-between; font-size: 0.8rem; color: var(--text-muted); margin-bottom: 4px;">
          <span>Classes Attended</span>
          <span><strong>${sub.attendedClasses}</strong> / ${sub.totalClasses}</span>
        </div>
        <div style="height: 8px; background: #f1f5f9; border-radius: 4px; overflow: hidden;">
          <div style="width: ${sub.percentage}%; height: 100%; background: var(--${colorClass});"></div>
        </div>
      </div>
    `;
    container.appendChild(card);
  });
}

// ===================================================================
// Student Portal: Attendance History
// ===================================================================

async function loadStudentHistoryView() {
  const user = state.currentUser;
  if (!user || !user.studentId) return;

  populateCourseSelects();
  const courseFilter = document.getElementById('historyCourseFilter').value;
  let url = API.attendanceHistory(user.studentId);
  if (courseFilter) url += `?courseId=${courseFilter}`;

  try {
    const res = await fetch(url);
    if (res.ok) {
      const records = await res.json();
      renderStudentHistoryTable(records);
    }
  } catch (e) {
    console.error('Failed to load history', e);
  }
}

function renderStudentHistoryTable(records) {
  const tbody = document.getElementById('studentHistoryTableBody');
  tbody.innerHTML = '';

  if (records.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 24px;" class="text-muted">No attendance logs found for this filter.</td></tr>';
    return;
  }

  records.forEach((r, idx) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${idx + 1}</td>
      <td><strong>${r.attendanceDate}</strong></td>
      <td><span class="badge badge-info">${r.courseCode}</span> ${r.courseName}</td>
      <td><span class="badge badge-${r.status.toLowerCase()}">${r.status}</span></td>
      <td class="text-muted">${r.remarks || 'Regular class'}</td>
    `;
    tbody.appendChild(tr);
  });
}

// ===================================================================
// UI Notification Toast
// ===================================================================

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${type === 'success' ? '✓' : (type === 'error' ? '⚠' : 'ℹ')}</span> <div>${message}</div>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}
