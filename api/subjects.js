const { db, parseBody, setCors } = require('./data');

module.exports = async (req, res) => {
  setCors(res);
  const method = req.method.toUpperCase();

  if (method === 'OPTIONS') {
    return res.status(204).end();
  }

  if (method === 'GET') {
    return res.status(200).json(db.subjects);
  }

  if (method === 'POST') {
    try {
      const data = await parseBody(req);
      const code = (data.code || '').toUpperCase().trim();
      const name = (data.name || '').trim();
      const instructor = (data.instructor || '').trim();

      if (!code || !name) {
        return res.status(400).json({ error: 'Subject code and name are required.' });
      }

      const exists = db.subjects.some(s => s.code.toUpperCase() === code);
      if (exists) {
        return res.status(409).json({ error: 'Subject code already exists.' });
      }

      const newSubject = {
        id: db.subjectNextId++,
        code,
        name,
        instructor
      };
      db.subjects.push(newSubject);

      return res.status(201).json({ success: true, message: 'Subject added.' });
    } catch (err) {
      return res.status(500).json({ error: err.message });
    }
  }

  return res.status(405).json({ error: 'Method Not Allowed' });
};
