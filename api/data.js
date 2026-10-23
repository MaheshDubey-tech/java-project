// Shared in-memory data store for Vercel Serverless Functions
const initialStudents = [
  { id: 1, rollNo: 'CS202401', name: 'Aarav Patel', email: 'aarav.patel@college.edu', phone: '9876543210', course: 'B.Tech CSE', semester: 'Semester 4', batch: '2024-2028' },
  { id: 2, rollNo: 'CS202402', name: 'Diya Dubey', email: 'diya.dubey@college.edu', phone: '9876543211', course: 'B.Tech CSE', semester: 'Semester 4', batch: '2024-2028' },
  { id: 3, rollNo: 'CS202403', name: 'Rohan Mehta', email: 'rohan.mehta@college.edu', phone: '9876543212', course: 'B.Tech CSE', semester: 'Semester 4', batch: '2024-2028' },
  { id: 4, rollNo: 'CS202404', name: 'Ananya Iyer', email: 'ananya.iyer@college.edu', phone: '9876543213', course: 'B.Tech CSE', semester: 'Semester 4', batch: '2024-2028' },
  { id: 5, rollNo: 'CS202405', name: 'Kabir Verma', email: 'kabir.verma@college.edu', phone: '9876543214', course: 'B.Tech CSE', semester: 'Semester 4', batch: '2024-2028' }
];

const initialSubjects = [
  { id: 1, code: 'CS101', name: 'Core Java Programming', instructor: 'Dr. Sharma' },
  { id: 2, code: 'CS102', name: 'Data Structures & Algorithms', instructor: 'Prof. Verma' },
  { id: 3, code: 'CS103', name: 'Database Management Systems', instructor: 'Dr. Gupta' },
  { id: 4, code: 'CS104', name: 'Web Technologies', instructor: 'Prof. Singh' }
];

const initialAttendance = [
  { id: 1, studentId: 1, subjectCode: 'CS101', date: '2026-10-08', status: 'PRESENT', remarks: 'On time' },
  { id: 2, studentId: 2, subjectCode: 'CS101', date: '2026-10-08', status: 'PRESENT', remarks: 'On time' },
  { id: 3, studentId: 3, subjectCode: 'CS101', date: '2026-10-08', status: 'ABSENT', remarks: 'Medical Leave' },
  { id: 4, studentId: 4, subjectCode: 'CS101', date: '2026-10-08', status: 'PRESENT', remarks: 'On time' },
  { id: 5, studentId: 5, subjectCode: 'CS101', date: '2026-10-08', status: 'PRESENT', remarks: 'On time' },
  { id: 6, studentId: 1, subjectCode: 'CS101', date: '2026-10-09', status: 'PRESENT', remarks: 'On time' },
  { id: 7, studentId: 2, subjectCode: 'CS101', date: '2026-10-09', status: 'PRESENT', remarks: 'On time' },
  { id: 8, studentId: 3, subjectCode: 'CS101', date: '2026-10-09', status: 'ABSENT', remarks: 'Unexcused' },
  { id: 9, studentId: 4, subjectCode: 'CS101', date: '2026-10-09', status: 'LATE', remarks: '10 mins late' },
  { id: 10, studentId: 5, subjectCode: 'CS101', date: '2026-10-09', status: 'PRESENT', remarks: 'On time' },
  { id: 11, studentId: 1, subjectCode: 'CS101', date: '2026-10-10', status: 'PRESENT', remarks: 'On time' },
  { id: 12, studentId: 2, subjectCode: 'CS101', date: '2026-10-10', status: 'PRESENT', remarks: 'On time' },
  { id: 13, studentId: 3, subjectCode: 'CS101', date: '2026-10-10', status: 'PRESENT', remarks: 'On time' },
  { id: 14, studentId: 4, subjectCode: 'CS101', date: '2026-10-10', status: 'PRESENT', remarks: 'On time' },
  { id: 15, studentId: 5, subjectCode: 'CS101', date: '2026-10-10', status: 'ABSENT', remarks: 'Sick' }
];

if (!global.__ATTENDANCE_DB__) {
  global.__ATTENDANCE_DB__ = {
    students: JSON.parse(JSON.stringify(initialStudents)),
    subjects: JSON.parse(JSON.stringify(initialSubjects)),
    attendance: JSON.parse(JSON.stringify(initialAttendance)),
    studentNextId: 6,
    subjectNextId: 5,
    attendanceNextId: 16
  };
}

const db = global.__ATTENDANCE_DB__;

function calculateReports(subjectCode = 'ALL') {
  const isAll = !subjectCode || subjectCode.toUpperCase() === 'ALL';
  return db.students.map(student => {
    const studentRecords = db.attendance.filter(a => {
      const matchStudent = a.studentId === student.id;
      const matchSubj = isAll || (a.subjectCode.toUpperCase() === subjectCode.toUpperCase());
      return matchStudent && matchSubj;
    });

    const totalClasses = studentRecords.length;
    let presentCount = 0;
    let absentCount = 0;
    let lateCount = 0;

    studentRecords.forEach(r => {
      const st = (r.status || '').toUpperCase();
      if (st === 'PRESENT') presentCount++;
      else if (st === 'LATE') lateCount++;
      else if (st === 'ABSENT') absentCount++;
    });

    const attended = presentCount + lateCount;
    const percentage = totalClasses > 0 ? Number(((attended / totalClasses) * 100).toFixed(2)) : 0.0;
    const isDefaulter = totalClasses > 0 && percentage < 75.0;

    return {
      studentId: student.id,
      rollNo: student.rollNo,
      name: student.name,
      course: student.course || '',
      subjectCode: isAll ? 'ALL' : subjectCode.toUpperCase(),
      totalClasses,
      presentCount,
      absentCount,
      lateCount,
      percentage,
      isDefaulter
    };
  });
}

module.exports = {
  db,
  calculateReports,
  parseBody: async function(req) {
    if (typeof req.body === 'object' && req.body !== null) {
      return req.body;
    }
    return new Promise((resolve) => {
      let data = '';
      req.on('data', chunk => { data += chunk; });
      req.on('end', () => {
        if (!data) return resolve({});
        try {
          resolve(JSON.parse(data));
        } catch {
          const params = new URLSearchParams(data);
          const obj = {};
          for (const [k, v] of params.entries()) {
            obj[k] = v;
          }
          resolve(obj);
        }
      });
    });
  },
  setCors: function(res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  }
};
