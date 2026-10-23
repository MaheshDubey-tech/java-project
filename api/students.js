const { db, parseBody, setCors } = require('./data');

module.exports = async (req, res) => {
  setCors(res);
  const method = req.method.toUpperCase();

  if (method === 'OPTIONS') {
    return res.status(204).end();
  }

  if (method === 'GET') {
    return res.status(200).json(db.students);
  }

  if (method === 'POST') {
    try {
      const data = await parseBody(req);
      const rollNo = (data.rollNo || '').toUpperCase().trim();
      const name = (data.name || '').trim();
      const email = (data.email || '').trim();
      const phone = (data.phone || '').trim();
      const course = (data.course || '').trim();
      const semester = (data.semester || '').trim();
      const batch = (data.batch || '').trim();

      if (!rollNo || !name) {
        return res.status(400).json({ error: 'Roll Number and Name are required.' });
      }

      const exists = db.students.some(s => s.rollNo.toUpperCase() === rollNo);
      if (exists) {
        return res.status(409).json({ error: `Student with Roll No '${rollNo}' already exists.` });
      }

      const newStudent = {
        id: db.studentNextId++,
        rollNo,
        name,
        email,
        phone,
        course,
        semester,
        batch
      };
      db.students.push(newStudent);

      return res.status(201).json({ success: true, message: 'Student added successfully.' });
    } catch (err) {
      return res.status(500).json({ error: err.message });
    }
  }

  if (method === 'DELETE') {
    try {
      const rollNo = req.query.rollNo || (new URL(req.url, 'http://localhost')).searchParams.get('rollNo');
      if (!rollNo) {
        return res.status(400).json({ error: 'rollNo parameter is required.' });
      }

      const index = db.students.findIndex(s => s.rollNo.toUpperCase() === rollNo.toUpperCase().trim());
      if (index === -1) {
        return res.status(404).json({ error: 'Student not found.' });
      }

      const [removed] = db.students.splice(index, 1);
      // Clean up attendance
      db.attendance = db.attendance.filter(a => a.studentId !== removed.id);

      return res.status(200).json({ success: true, message: 'Student deleted successfully.' });
    } catch (err) {
      return res.status(500).json({ error: err.message });
    }
  }

  return res.status(405).json({ error: 'Method Not Allowed' });
};
