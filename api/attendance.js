const { db, parseBody, setCors } = require('./data');

module.exports = async (req, res) => {
  setCors(res);
  const method = req.method.toUpperCase();

  if (method === 'OPTIONS') {
    return res.status(204).end();
  }

  const url = new URL(req.url, 'http://localhost');

  if (method === 'GET') {
    const subject = (req.query?.subject || url.searchParams.get('subject') || 'CS101').toUpperCase();
    const date = req.query?.date || url.searchParams.get('date') || new Date().toISOString().split('T')[0];

    const records = db.attendance
      .filter(a => a.subjectCode.toUpperCase() === subject && a.date === date)
      .map(a => {
        const student = db.students.find(s => s.id === a.studentId) || {};
        return {
          id: a.id,
          studentId: a.studentId,
          rollNo: student.rollNo || '',
          name: student.name || '',
          subjectCode: a.subjectCode,
          date: a.date,
          status: a.status,
          remarks: a.remarks || ''
        };
      });

    return res.status(200).json(records);
  }

  if (method === 'POST') {
    try {
      const data = await parseBody(req);
      const subject = (data.subjectCode || '').toUpperCase().trim();
      const date = data.date || new Date().toISOString().split('T')[0];

      if (data.entries) {
        // Batch: "studentId:STATUS;studentId:STATUS"
        const pairs = data.entries.split(';');
        let count = 0;

        for (const p of pairs) {
          if (!p.trim()) continue;
          const [sIdStr, status] = p.split(':');
          if (sIdStr && status) {
            const studentId = parseInt(sIdStr.trim(), 10);
            const st = status.trim().toUpperCase();

            const existingIndex = db.attendance.findIndex(
              a => a.studentId === studentId && a.subjectCode === subject && a.date === date
            );

            if (existingIndex !== -1) {
              db.attendance[existingIndex].status = st;
            } else {
              db.attendance.push({
                id: db.attendanceNextId++,
                studentId,
                subjectCode: subject,
                date,
                status: st,
                remarks: ''
              });
            }
            count++;
          }
        }

        return res.status(200).json({ success: true, marked: count });
      } else {
        // Single
        const studentId = parseInt(data.studentId, 10);
        const status = (data.status || 'PRESENT').toUpperCase();
        const remarks = data.remarks || '';

        const existingIndex = db.attendance.findIndex(
          a => a.studentId === studentId && a.subjectCode === subject && a.date === date
        );

        if (existingIndex !== -1) {
          db.attendance[existingIndex].status = status;
          db.attendance[existingIndex].remarks = remarks;
        } else {
          db.attendance.push({
            id: db.attendanceNextId++,
            studentId,
            subjectCode: subject,
            date,
            status,
            remarks
          });
        }

        return res.status(200).json({ success: true });
      }
    } catch (err) {
      return res.status(500).json({ error: err.message });
    }
  }

  return res.status(405).json({ error: 'Method Not Allowed' });
};
